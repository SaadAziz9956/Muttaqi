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

final class CompassService: NSObject, CompassServiceProtocol, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var continuation: AsyncStream<CompassHeading>.Continuation?

    var isAvailable: Bool {
        CLLocationManager.headingAvailable()
    }

    func headings() -> AsyncStream<CompassHeading> {
        AsyncStream { continuation in
            self.continuation = continuation
            manager.delegate = self
            manager.headingFilter = 1
            manager.startUpdatingHeading()
            // Stops the compass once whoever is reading the stream goes away
            continuation.onTermination = { _ in
                Task { @MainActor [weak self] in self?.manager.stopUpdatingHeading() }
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        let degrees = newHeading.trueHeading >= 0 ? newHeading.trueHeading : newHeading.magneticHeading
        continuation?.yield(CompassHeading(degrees: degrees, accuracy: newHeading.headingAccuracy))
    }
}
