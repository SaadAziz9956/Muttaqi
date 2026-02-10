import SwiftUI

@main
struct MuttaqiApp: App {
    @State private var appViewModel: AppViewModel
    private let userPreferences: UserPreferences
    
    init() {
        let prefs = UserPreferences()
        self.userPreferences = prefs
        self._appViewModel = State(initialValue: AppViewModel(userPreferences: prefs))
    }
    
    var body: some Scene {
        WindowGroup {
            switch appViewModel.state {
            case .splash:
                SplashView(isOnboardingComplete: userPreferences.isOnboardingComplete())
                    .task {
                        await appViewModel.initialize()
                    }
            case .onboarding:
                makeOnboardingView()
            case .home:
                MainTabView()
            }
        }
    }
    
    private func makeOnboardingView() -> OnboardingView {
        let viewModel = OnboardingViewModel(
            userPreferences: userPreferences,
            notificationService: NotificationService(),
            locationService: LocationService()
        )
        viewModel.onOnboardingComplete = {
            appViewModel.onboardingCompleted()
        }
        return OnboardingView(viewModel: viewModel)
    }
}
