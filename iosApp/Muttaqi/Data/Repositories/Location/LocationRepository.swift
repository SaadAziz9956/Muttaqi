import CoreLocation
import Foundation

final class LocationRepository: LocationRepositoryProtocol {
    private let service: LocationServiceProtocol
    private let preferences: UserPreferencesProtocol

    init(service: LocationServiceProtocol, preferences: UserPreferencesProtocol) {
        self.service = service
        self.preferences = preferences
    }

    var access: LocationAccess {
        switch service.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways: .granted
        case .denied, .restricted: .denied
        default: .notDetermined
        }
    }

    func requestAccess() async -> LocationAccess {
        await service.requestPermission() ? .granted : access
    }

    func lastKnownCoordinates() -> Coordinates? {
        preferences.getLastKnownCoordinates()
    }

    func refreshCoordinates() async -> Coordinates? {
        guard access == .granted,
              let location = await service.currentLocation(timeout: .seconds(10)) else { return nil }
        let coordinates = Coordinates(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)
        preferences.setLastKnownCoordinates(coordinates)
        return coordinates
    }
}
