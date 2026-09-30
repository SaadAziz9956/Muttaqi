import Foundation
import Shared

@Observable
@MainActor
final class AppViewModel {
    enum AppState: Equatable {
        case splash
        case onboarding
        case home
    }
    
    private(set) var state: AppState = .splash
    
    private let isOnboardingComplete: () -> Bool

    init(isOnboardingComplete: @escaping () -> Bool = { OnboardingStatus.shared.isComplete() }) {
        self.isOnboardingComplete = isOnboardingComplete
    }
    
    func initialize() async {
        try? await Task.sleep(for: .seconds(2))
        
        if isOnboardingComplete() {
            state = .home
        } else {
            state = .onboarding
        }
    }
    
    func onboardingCompleted() {
        state = .home
    }
}
