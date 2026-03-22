import CoreLocation

protocol LocationServiceProtocol: Sendable {
    func requestPermission() async -> Bool
}

final class LocationService: NSObject, LocationServiceProtocol, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<Bool, Never>?
    
    override init() {
        super.init()
        manager.delegate = self
    }
    
    func requestPermission() async -> Bool {
        await withCheckedContinuation { continuation in
            self.continuation = continuation
            manager.requestWhenInUseAuthorization()
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let continuation else { return }
        switch manager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            continuation.resume(returning: true)
        case .denied, .restricted:
            continuation.resume(returning: false)
        default:
            return
        }
        self.continuation = nil
    }
}
