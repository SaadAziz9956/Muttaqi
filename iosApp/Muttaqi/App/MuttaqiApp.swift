import Shared
import SwiftUI

@main
struct MuttaqiApp: App {
    @State private var appViewModel: AppViewModel
    private let container: DependencyContainer

    init() {
        let container = DependencyContainer()
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
                        await container.prepareQuran()
                        async let content: Void = container.prepareContent()
                        await appViewModel.initialize()
                        await content
                    }
            case .onboarding:
                OnboardingView { appViewModel.onboardingCompleted() }
            case .home:
                MainTabView()
            }
        }
    }
}
