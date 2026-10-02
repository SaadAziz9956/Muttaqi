import Shared
import SwiftUI

struct OnboardingView: View {
    @State private var screen = SharedViewModel(OnboardingViewModels.shared.onboarding()) { $0.state }
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let onComplete: () -> Void

    private var state: OnboardingState { screen.state }

    var body: some View {
        VStack(spacing: 0) {
            if state.step != .setup {
                Text("متقي")
                    .font(.custom("ReemKufi-Regular", size: 60))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 48)
                    .transition(.opacity)
            }

            ZStack {
                stepView
                    .id(state.step)
                    .transition(stepTransition)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .animation(.smooth(duration: 0.45), value: state.step)
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .finished: onComplete()
                }
            }
        }
    }

    private var stepTransition: AnyTransition {
        reduceMotion ? .opacity : .push(from: .trailing)
    }

    private func dispatch(_ intent: OnboardingIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    @ViewBuilder
    private var stepView: some View {
        switch state.step {
        case .welcome:
            WelcomeStepView {
                dispatch(OnboardingIntentBegin.shared)
            }
        case .name:
            NameStepView(
                name: Binding(
                    get: { screen.viewModel.state.value.name },
                    set: { dispatch(OnboardingIntentNameChanged(name: $0)) }
                ),
                onSave: { dispatch(OnboardingIntentSaveName.shared) }
            )
        case .goals:
            GoalsStepView {
                dispatch(OnboardingIntentNext.shared)
            }
        case .notification:
            NotificationStepView(
                onEnable: { dispatch(OnboardingIntentRequestNotification.shared) },
                onSkip: { dispatch(OnboardingIntentSkipNotification.shared) }
            )
        case .location:
            LocationStepView(
                onFind: { dispatch(OnboardingIntentRequestLocation.shared) },
                onSkip: { dispatch(OnboardingIntentSkipLocation.shared) }
            )
        case .setup:
            SetupStepView(
                errorMessage: state.setupError,
                onRetry: { dispatch(OnboardingIntentRetrySetup.shared) }
            )
        }
    }
}
