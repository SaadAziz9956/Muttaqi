import Foundation

protocol UserPreferencesProtocol: Sendable {
    func setUserName(_ name: String)
    func getUserName() -> String?
    func setOnboardingComplete(_ complete: Bool)
    func isOnboardingComplete() -> Bool
    func setLastKnownCoordinates(_ coordinates: Coordinates)
    func getLastKnownCoordinates() -> Coordinates?
}

final class UserPreferences: UserPreferencesProtocol, @unchecked Sendable {
    private let defaults: UserDefaults
    
    private enum Keys {
        static let userName = "user_name"
        static let onboardingComplete = "onboarding_complete"
        static let lastKnownCoordinates = "last_known_coordinates"
    }
    
    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }
    
    func setUserName(_ name: String) {
        defaults.set(name, forKey: Keys.userName)
    }
    
    func getUserName() -> String? {
        defaults.string(forKey: Keys.userName)
    }
    
    func setOnboardingComplete(_ complete: Bool) {
        defaults.set(complete, forKey: Keys.onboardingComplete)
    }
    
    func isOnboardingComplete() -> Bool {
        defaults.bool(forKey: Keys.onboardingComplete)
    }

    func setLastKnownCoordinates(_ coordinates: Coordinates) {
        defaults.set(try? JSONEncoder().encode(coordinates), forKey: Keys.lastKnownCoordinates)
    }

    func getLastKnownCoordinates() -> Coordinates? {
        guard let data = defaults.data(forKey: Keys.lastKnownCoordinates) else { return nil }
        return try? JSONDecoder().decode(Coordinates.self, from: data)
    }
}
