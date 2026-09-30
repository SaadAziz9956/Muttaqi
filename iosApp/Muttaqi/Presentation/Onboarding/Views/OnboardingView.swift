import SwiftUI

struct OnboardingView: View {
    @State private var viewModel: OnboardingViewModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    init(viewModel: OnboardingViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        VStack(spacing: 0) {
            // Shared by every step so it stays still while the step content changes; setup centres its own
            if viewModel.currentStep != .setup {
                Text("متقي")
                    .font(.custom("ReemKufi-Regular", size: 60))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 48)
                    .transition(.opacity)
            }

            ZStack {
                stepView
                    .id(viewModel.currentStep)
                    .transition(stepTransition)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .animation(.smooth(duration: 0.45), value: viewModel.currentStep)
    }

    // Steps advance like a navigation push; with Reduce Motion on they cross-fade instead
    private var stepTransition: AnyTransition {
        reduceMotion ? .opacity : .push(from: .trailing)
    }

    @ViewBuilder
    private var stepView: some View {
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
            SetupStepView(
                errorMessage: viewModel.setupError,
                onRetry: { viewModel.send(.finishSetup) }
            )
            .task {
                viewModel.send(.finishSetup)
            }
        }
    }
}
