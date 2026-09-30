import CoreLocation
import Shared

/// The compass for the shared code, on Core Location's heading
nonisolated final class CompassService: NSObject, HeadingProvider {
    var isAvailable: Bool {
        CLLocationManager.headingAvailable()
    }

    func startUpdates(onHeading: @escaping (CompassHeading) -> Void) -> HeadingUpdates {
        // Each caller has its own location manager, so one stopping can never stop another
        HeadingTracker(onHeading: onHeading)
    }
}

private nonisolated final class HeadingTracker: NSObject, HeadingUpdates, CLLocationManagerDelegate {
    private var manager: CLLocationManager?
    private let onHeading: (CompassHeading) -> Void

    init(onHeading: @escaping (CompassHeading) -> Void) {
        self.onHeading = onHeading
        super.init()
        onMain { [self] in start() }
    }

    private func start() {
        let manager = CLLocationManager()
        self.manager = manager
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
        onMain { [self] in
            manager?.stopUpdatingHeading()
            manager?.stopUpdatingLocation()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        let degrees = newHeading.trueHeading >= 0 ? newHeading.trueHeading : newHeading.magneticHeading
        onHeading(CompassHeading(degrees: degrees, accuracy: newHeading.headingAccuracy))
    }
}
