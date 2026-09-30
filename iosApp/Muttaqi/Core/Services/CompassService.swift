import CoreLocation

struct CompassHeading: Equatable, Sendable {
    /// Degrees clockwise from true north (magnetic north when true north isn't available)
    let degrees: Double
    /// Error estimate in degrees; negative when the compass needs calibrating
    let accuracy: Double
}

protocol CompassServiceProtocol: Sendable {
    var isAvailable: Bool { get }
    func headings() -> AsyncStream<CompassHeading>
}

struct CompassService: CompassServiceProtocol {
    var isAvailable: Bool {
        CLLocationManager.headingAvailable()
    }

    func headings() -> AsyncStream<CompassHeading> {
        AsyncStream { continuation in
            // Each stream has its own location manager, so an old stream finishing can never stop a newer one
            let tracker = HeadingTracker(continuation: continuation)
            // Stops the compass once whoever is reading the stream goes away
            continuation.onTermination = { _ in
                Task { @MainActor in tracker.stop() }
            }
        }
    }
}

private final class HeadingTracker: NSObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private let continuation: AsyncStream<CompassHeading>.Continuation

    init(continuation: AsyncStream<CompassHeading>.Continuation) {
        self.continuation = continuation
        super.init()
        manager.delegate = self
        manager.headingFilter = 1
        // Core Location only reports true north while this manager also has a location; a rough one is enough
        if [.authorizedWhenInUse, .authorizedAlways].contains(manager.authorizationStatus) {
            manager.desiredAccuracy = kCLLocationAccuracyThreeKilometers
            manager.startUpdatingLocation()
        }
        manager.startUpdatingHeading()
    }

    func stop() {
        manager.stopUpdatingHeading()
        manager.stopUpdatingLocation()
    }

    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        let degrees = newHeading.trueHeading >= 0 ? newHeading.trueHeading : newHeading.magneticHeading
        continuation.yield(CompassHeading(degrees: degrees, accuracy: newHeading.headingAccuracy))
    }
}
