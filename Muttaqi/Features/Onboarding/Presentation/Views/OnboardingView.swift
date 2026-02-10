import SwiftUI

struct OnboardingView: View {
    @State private var viewModel: OnboardingViewModel
    
    init(viewModel: OnboardingViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }
    
    var body: some View {
        Group {
            switch viewModel.currentStep {
            case .welcome:
                WelcomeStepView {
                    viewModel.send(.begin)
                }
            case .name:
                NameStepView { name in
                    viewModel.send(.saveName(name))
                }
            case .goals:
                GoalsStepView {
                    viewModel.send(.next)
                }
            case .notification:
                NotificationStepView(
                    onEnable: { viewModel.send(.requestNotification) },
                    onSkip: { viewModel.send(.skipNotification) }
                )
            case .location:
                LocationStepView(
                    onFind: { viewModel.send(.requestLocation) },
                    onSkip: { viewModel.send(.skipLocation) }
                )
            case .setup:
                SetupStepView()
                    .task {
                        viewModel.send(.finishSetup)
                    }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.currentStep)
    }
}
