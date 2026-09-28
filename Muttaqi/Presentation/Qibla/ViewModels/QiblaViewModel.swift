import Foundation

@Observable
@MainActor
final class QiblaViewModel {
    enum State: Equatable {
        case locating
        case needsLocation(LocationAccess)
        case ready
    }

    private(set) var state: State = .locating
    private(set) var qibla: QiblaDirection?
    private(set) var heading: CompassHeading?
    /// Heading as a continuous angle (it can pass 360), so turning past north animates the short way round
    private(set) var dialRotation: Double = 0

    private let location: LocationRepositoryProtocol
    private let compass: CompassServiceProtocol
    private let getQiblaDirection: GetQiblaDirectionUseCase

    init(location: LocationRepositoryProtocol, compass: CompassServiceProtocol, getQiblaDirection: GetQiblaDirectionUseCase) {
        self.location = location
        self.compass = compass
        self.getQiblaDirection = getQiblaDirection
    }

    var isCompassAvailable: Bool { compass.isAvailable }

    /// Degrees to turn to face the Kaaba: positive means turn right
    var turnAngle: Double? {
        guard let qibla, let heading else { return nil }
        let delta = (qibla.bearing - heading.degrees).truncatingRemainder(dividingBy: 360)
        return delta > 180 ? delta - 360 : (delta < -180 ? delta + 360 : delta)
    }

    var isAligned: Bool {
        guard let turnAngle else { return false }
        return abs(turnAngle) <= 3
    }

    var needsCalibration: Bool {
        guard let heading else { return false }
        return heading.accuracy < 0 || heading.accuracy > 25
    }

    func locate() async {
        if let saved = location.lastKnownCoordinates() {
            qibla = getQiblaDirection.execute(from: saved)
            state = .ready
        }
        if location.access == .granted, let fresh = await location.refreshCoordinates() {
            qibla = getQiblaDirection.execute(from: fresh)
            state = .ready
        }
        if qibla == nil {
            state = .needsLocation(location.access)
        }
    }

    func requestLocation() async {
        _ = await location.requestAccess()
        await locate()
    }

    /// Follows the compass until the calling task is cancelled
    func trackHeading() async {
        guard compass.isAvailable else { return }
        for await update in compass.headings() {
            let previous = heading?.degrees ?? update.degrees
            var step = (update.degrees - previous).truncatingRemainder(dividingBy: 360)
            if step > 180 { step -= 360 } else if step < -180 { step += 360 }
            dialRotation = heading == nil ? update.degrees : dialRotation + step
            heading = update
        }
    }
}
