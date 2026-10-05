import Shared
import SwiftUI

struct DhikrCounterView: View {
    @State private var screen: SharedViewModel<DhikrCounterViewModel, DhikrCounterState>
    @State private var isConfirmingReset = false
    @State private var tap = CounterTap()
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss
    @ScaledMetric(relativeTo: .largeTitle) private var counterSize: CGFloat = 164

    init(dhikrId: String) {
        _screen = State(initialValue: SharedViewModel(DhikrViewModels.shared.counter(id: dhikrId)) { $0.state })
    }

    private var state: DhikrCounterState { screen.state }

    var body: some View {
        Group {
            if let dhikr = state.dhikr {
                content(dhikr)
            } else {
                Color.clear.background { SoftBackdrop() }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .background(SwipeBackEnabler())
        .toolbar(.hidden, for: .tabBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button { dismiss() } label: {
                    Image("arrow-left-02-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.textPrimary)
                }
                .accessibilityLabel("Back")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { dispatch(DhikrCounterIntentShareTapped.shared) } label: {
                    Image("export-arrow-01-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                        .contentShape(.rect)
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityLabel("Share")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { isConfirmingReset = true } label: {
                    Image("refresh-left-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
                .disabled(!state.hasProgress)
                .accessibilityLabel("Reset count")
                .confirmationDialog("Reset today's count?", isPresented: $isConfirmingReset, titleVisibility: .visible) {
                    Button("Reset", role: .destructive) {
                        dispatch(DhikrCounterIntentResetConfirmed.shared)
                    }
                }
            }
        }
        .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
        .onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .counted(let counted): tap = CounterTap(sequence: tap.sequence + 1, milestone: counted.milestone)
                case .openShare(let share): router.push(share.passage)
                }
            }
        }
    }

    private func dispatch(_ intent: DhikrCounterIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private func content(_ dhikr: Dhikr) -> some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    if let title = dhikr.title {
                        Text(title)
                            .font(.labelMedium)
                            .foregroundStyle(.brandTeal)
                            .padding(.top, 8)
                    }

                    if dhikr.steps.isEmpty {
                        phrase(dhikr)
                    } else {
                        steps(dhikr)
                    }

                    if let hadith = dhikr.hadith {
                        hadithCard(hadith)
                            .padding(.top, 28)
                    }

                    source(dhikr)
                        .padding(.top, 16)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
            .background { SoftBackdrop() }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                counter(dhikr)
            }
            .onChange(of: state.currentStep?.index) { _, index in
                guard let index else { return }
                withAnimation(.snappy) { proxy.scrollTo(Int(index), anchor: .center) }
            }
        }
        .animation(state.hasProgress ? .snappy(duration: 0.2) : .snappy, value: state.progress)
    }

    private func phrase(_ dhikr: Dhikr) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(AttributedString.arabic(dhikr.arabic, size: 28))
                .lineSpacing(12)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)

            if let transliteration = dhikr.transliteration {
                Text(transliteration)
                    .font(.custom("ReemKufi-Regular", size: 15, relativeTo: .body))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 20)
            }

            if let translation = dhikr.translation {
                translated(translation, size: 15, color: .textPrimary)
                    .padding(.top, 10)
            }
        }
        .padding(20)
        .softCard(cornerRadius: 26)
        .padding(.top, 16)
        .textSelection(.enabled)
    }

    private func steps(_ dhikr: Dhikr) -> some View {
        let current = Int(state.currentStep?.index ?? 0)

        return VStack(spacing: 12) {
            ForEach(Array(dhikr.steps.enumerated()), id: \.offset) { index, step in
                VStack(alignment: .leading, spacing: 6) {
                    HStack(alignment: .firstTextBaseline) {
                        Text("\(step.count)×")
                            .font(.custom("ReemKufi-Medium", size: 13))
                            .foregroundStyle(.brandTeal)
                        Spacer(minLength: 12)
                        Text(AttributedString.arabic(step.arabic, size: 22))
                            .foregroundStyle(.textPrimary)
                            .multilineTextAlignment(.trailing)
                    }
                    if let transliteration = step.transliteration {
                        Text(transliteration)
                            .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .body))
                            .foregroundStyle(.appPrimary)
                    }
                    if let translation = step.translation {
                        translated(translation, size: 13, color: .textSecondary)
                    }
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .softCard(cornerRadius: 22)
                .overlay {
                    RoundedRectangle(cornerRadius: 22, style: .continuous)
                        .strokeBorder(Color.shareCard, lineWidth: 2)
                        .opacity(index == current ? 1 : 0)
                }
                .opacity(index < current ? 0.45 : 1)
                .id(index)
                .accessibilityElement(children: .combine)
            }
        }
        .padding(.top, 16)
        .animation(.snappy, value: current)
    }

    private func hadithCard(_ hadith: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Hadith")
                .font(.labelMedium)
                .foregroundStyle(.brandTeal)
            translated(hadith, size: 15, color: .textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .softCard(cornerRadius: 24)
    }

    private func translated(_ text: String, size: CGFloat, color: Color) -> some View {
        let style = TranslationStyle(for: text, size: size)
        return Text(text)
            .font(style.font)
            .foregroundStyle(color)
            .lineSpacing(style.isRightToLeft ? 8 : 4)
            .multilineTextAlignment(style.isRightToLeft ? .trailing : .leading)
            .frame(maxWidth: .infinity, alignment: style.isRightToLeft ? .trailing : .leading)
    }

    private func source(_ dhikr: Dhikr) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(dhikr.reference)
                .font(.labelSmall)
                .foregroundStyle(.brandTeal)
            Text(dhikr.grade)
                .font(.system(size: 11))
                .foregroundStyle(.textSecondary)
            if let credit = dhikr.credit {
                Text("Translation: \(credit)")
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
            }
        }
        .accessibilityElement(children: .combine)
    }

    private func counter(_ dhikr: Dhikr) -> some View {
        VStack(spacing: 10) {
            if let step = state.currentStep {
                let phrase = dhikr.steps[Int(step.index)]
                Text([phrase.transliteration, "\(step.said) of \(phrase.count)"].compactMap { $0 }.joined(separator: " · "))
                    .font(.labelLarge)
                    .foregroundStyle(.appPrimary)
                    .contentTransition(.numericText())
            }

            Button {
                dispatch(DhikrCounterIntentCounted.shared)
            } label: {
                ZStack {
                    Circle()
                        .stroke(Color.brandTeal.opacity(0.18), lineWidth: 8)
                        .padding(10)
                    Circle()
                        .trim(from: 0, to: state.roundProgress)
                        .stroke(Color.shareCard, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                        .padding(10)

                    VStack(spacing: 0) {
                        Text("\(state.count)")
                            .font(.custom("ReemKufi-Medium", size: 46, relativeTo: .largeTitle))
                            .foregroundStyle(state.isRoundComplete ? Color.white : Color.appPrimary)
                            .contentTransition(.numericText())
                        Text(counterCaption)
                            .font(.labelSmall)
                            .foregroundStyle(state.isRoundComplete ? Color.white.opacity(0.85) : Color.textSecondary)
                    }
                }
                .frame(width: counterSize, height: counterSize)
                .softGlass(in: Circle(), fill: state.isRoundComplete ? .shareCard : .softSurface, rim: !state.isRoundComplete)
            }
            .buttonStyle(CounterButtonStyle())
            .sensoryFeedback(trigger: tap) { _, tap in
                guard tap.sequence > 0 else { return nil }
                switch tap.milestone {
                case .repetition: return .impact(weight: .light)
                case .phraseFinished: return .impact(weight: .heavy)
                case .roundFinished: return .success
                }
            }
            .accessibilityLabel("Count")
            .accessibilityValue(counterAccessibilityValue)
            .accessibilityHint("Double-tap to count one")

            Text(state.rounds == 1 ? "Completed once today" : "Completed \(state.rounds) times today")
                .font(.labelSmall)
                .foregroundStyle(.brandTeal)
                .opacity(state.rounds > 0 ? 1 : 0)
                .accessibilityHidden(state.rounds == 0)
        }
        .padding(.top, 14)
        .padding(.bottom, 6)
        .frame(maxWidth: .infinity)
        .background {
            LinearGradient(
                stops: [
                    .init(color: Color.softCanvas.opacity(0), location: 0),
                    .init(color: Color.softCanvas.opacity(0.92), location: 0.3),
                    .init(color: Color.softCanvas, location: 1),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .padding(.top, -30)
            .ignoresSafeArea(edges: .bottom)
            .allowsHitTesting(false)
        }
    }

    private var counterCaption: String {
        guard let target = state.target else {
            return state.count == 0 ? "Tap to count" : "times"
        }
        return state.isRoundComplete ? "Complete" : "of \(target.intValue)"
    }

    private var counterAccessibilityValue: String {
        guard let target = state.target else { return "\(state.count)" }
        return "\(state.count) of \(target.intValue)"
    }
}

private struct CounterTap: Equatable {
    var sequence = 0
    var milestone: DhikrMilestone = .repetition
}

private struct CounterButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
            .animation(.snappy(duration: 0.15), value: configuration.isPressed)
    }
}
