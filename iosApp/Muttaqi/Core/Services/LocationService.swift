import CoreLocation
import Shared

nonisolated final class LocationService: NSObject, LocationProvider, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var pendingAccess: ((LocationAccess) -> Void)?

    override init() {
        super.init()
        manager.delegate = self
    }

    var access: LocationAccess {
        switch manager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways: .granted
        case .denied, .restricted: .denied
        default: .notDetermined
        }
    }

    func requestAccess(onResult: @escaping (LocationAccess) -> Void) {
        onMain { [self] in
            guard manager.authorizationStatus == .notDetermined else { return onResult(access) }
            pendingAccess = onResult
            manager.requestWhenInUseAuthorization()
        }
    }

    func currentLocation(onResult: @escaping (Coordinates?) -> Void) -> LocationRequest {
        LocationFix(task: Task {
            do {
                for try await update in CLLocationUpdate.liveUpdates() {
                    if let location = update.location {
                        return onResult(Coordinates(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude))
                    }
                    if update.authorizationDenied || update.authorizationDeniedGlobally { return onResult(nil) }
                }
            } catch {}
            if !Task.isCancelled { onResult(nil) }
        })
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let pendingAccess, manager.authorizationStatus != .notDetermined else { return }
        self.pendingAccess = nil
        pendingAccess(access)
    }
}

private nonisolated final class LocationFix: NSObject, LocationRequest {
    private let task: Task<Void, Never>

    init(task: Task<Void, Never>) {
        self.task = task
    }

    func cancel() {
        task.cancel()
    }
}

nonisolated func onMain(_ work: @escaping @MainActor () -> Void) {
    if Thread.isMainThread {
        MainActor.assumeIsolated(work)
    } else {
        DispatchQueue.main.async(execute: work)
    }
}
