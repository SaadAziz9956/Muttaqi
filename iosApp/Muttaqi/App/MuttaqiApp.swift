import Shared
import SwiftData
import SwiftUI

@main
struct MuttaqiApp: App {
    @State private var appViewModel: AppViewModel
    private let container: DependencyContainer

    init() {
        // The shared (Kotlin) code's dependency injection, before any shared view model is made
        IosKoinKt.doInitKoinIos()
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
                        // The Quran moved to shared code: bring over what SwiftData kept before Home reads it
                        await container.prepareQuran()
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
