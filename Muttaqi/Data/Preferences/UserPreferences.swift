import Foundation

protocol UserPreferencesProtocol: Sendable {
    func setUserName(_ name: String)
    func getUserName() -> String?
    func setOnboardingComplete(_ complete: Bool)
    func isOnboardingComplete() -> Bool
}

final class UserPreferences: UserPreferencesProtocol, @unchecked Sendable {
    private let defaults: UserDefaults
    
    private enum Keys {
        static let userName = "user_name"
        static let onboardingComplete = "onboarding_complete"
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
}
