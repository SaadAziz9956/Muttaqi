import Foundation

@Observable
@MainActor
final class AppViewModel {
    enum AppState: Equatable {
        case splash
        case onboarding
        case home
    }
    
    private(set) var state: AppState = .splash
    
    private let userPreferences: UserPreferencesProtocol
    
    init(userPreferences: UserPreferencesProtocol) {
        self.userPreferences = userPreferences
    }
    
    func initialize() async {
        try? await Task.sleep(for: .seconds(2))
        
        if userPreferences.isOnboardingComplete() {
            state = .home
        } else {
            state = .onboarding
        }
    }
    
    func onboardingCompleted() {
        state = .home
    }
}
