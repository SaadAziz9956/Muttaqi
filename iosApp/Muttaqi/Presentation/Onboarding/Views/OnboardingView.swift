import Shared
import SwiftUI

struct OnboardingView: View {
    @State private var screen = SharedViewModel(OnboardingViewModels.shared.onboarding()) { $0.state }
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let onComplete: () -> Void

    private var state: OnboardingState { screen.state }

    var body: some View {
        VStack(spacing: 0) {
            // Shared by every step so it stays still while the step content changes; setup centres its own
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

    // Steps advance like a navigation push; with Reduce Motion on they cross-fade instead
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
            // Read from the view model itself, as the drawn state can lag a fast typist
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
            // The shared view model starts the download as this step opens
            SetupStepView(
                errorMessage: state.setupError,
                onRetry: { dispatch(OnboardingIntentRetrySetup.shared) }
            )
        }
    }
}
