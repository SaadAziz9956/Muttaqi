import SwiftUI
import SwiftData

@main
struct MuttaqiApp: App {
    @State private var appViewModel: AppViewModel
    private let container: DependencyContainer

    init() {
        let container = DependencyContainer()
        self.container = container
        self._appViewModel = State(initialValue: AppViewModel(userPreferences: container.userPreferences))
    }

    var body: some Scene {
        WindowGroup {
            switch appViewModel.state {
            case .splash:
                SplashView(isOnboardingComplete: container.userPreferences.isOnboardingComplete())
                    .task {
                        await appViewModel.initialize()
                    }
            case .onboarding:
                makeOnboardingView()
            case .home:
                MainTabView()
                    .environment(\.container, container)
            }
        }
        .modelContainer(container.modelContainer)
    }


    private func makeOnboardingView() -> OnboardingView {
        let viewModel = container.makeOnboardingViewModel()
        viewModel.onOnboardingComplete = {
            appViewModel.onboardingCompleted()
        }
        return OnboardingView(viewModel: viewModel)
    }
}
