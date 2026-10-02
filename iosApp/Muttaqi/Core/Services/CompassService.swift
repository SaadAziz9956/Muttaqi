import CoreLocation
import Shared

nonisolated final class CompassService: NSObject, HeadingProvider {
    var isAvailable: Bool {
        CLLocationManager.headingAvailable()
    }

    func startUpdates(onHeading: @escaping (CompassHeading) -> Void) -> HeadingUpdates {
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
