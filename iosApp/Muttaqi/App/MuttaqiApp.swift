import Shared
import SwiftUI

@main
struct MuttaqiApp: App {
    @State private var appViewModel: AppViewModel
    private let container: DependencyContainer

    init() {
        let container = DependencyContainer()
        // The shared (Kotlin) code's dependency injection, before any shared view model is made, with the services
        // that stay in Swift: Core Location, the compass and notifications
        IosKoinKt.doInitKoinIos(location: LocationService(), compass: CompassService(), notifications: NotificationService())
        self.container = container
        self._appViewModel = State(initialValue: AppViewModel())
    }

    var body: some Scene {
        WindowGroup {
            switch appViewModel.state {
            case .splash:
                SplashView(isOnboardingComplete: OnboardingStatus.shared.isComplete())
                    .task {
                        // The Quran moved to shared code: bring over what SwiftData kept before Home reads it
                        await container.prepareQuran()
                        await appViewModel.initialize()
                    }
            case .onboarding:
                OnboardingView { appViewModel.onboardingCompleted() }
            case .home:
                MainTabView()
            }
        }
    }
}
