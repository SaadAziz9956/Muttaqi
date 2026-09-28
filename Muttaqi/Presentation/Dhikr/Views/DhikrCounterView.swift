import SwiftUI

/// One dhikr in full, with a counter pinned at the bottom within reach of the thumb
struct DhikrCounterView: View {
    @State private var viewModel: DhikrCounterViewModel
    @State private var isConfirmingReset = false
    @Environment(\.dismiss) private var dismiss
    @ScaledMetric(relativeTo: .largeTitle) private var counterSize: CGFloat = 164

    init(viewModel: DhikrCounterViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    private var dhikr: Dhikr { viewModel.dhikr }

    var body: some View {
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
                        phrase
                    } else {
                        steps
                    }

                    if let hadith = dhikr.hadith {
                        hadithCard(hadith)
                            .padding(.top, 28)
                    }

                    source
                        .padding(.top, 16)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                counter
            }
            // Keeps the phrase to say now in view as a set moves on to the next one
            .onChange(of: viewModel.currentStep?.index) { _, index in
                guard let index else { return }
                withAnimation(.snappy) { proxy.scrollTo(index, anchor: .center) }
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
                Button { isConfirmingReset = true } label: {
                    Image("refresh-left-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
                .disabled(!viewModel.hasProgress)
                .accessibilityLabel("Reset count")
                .confirmationDialog("Reset today's count?", isPresented: $isConfirmingReset, titleVisibility: .visible) {
                    Button("Reset", role: .destructive) {
                        withAnimation(.snappy) { viewModel.reset() }
                    }
                }
            }
        }
        // Counting a long set shouldn't be interrupted by the screen locking
        .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
        .onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
    }

    // MARK: - Text

    private var phrase: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(AttributedString.arabic(dhikr.arabic, size: 28))
                .lineSpacing(12)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 16)

            Text(dhikr.transliteration)
                .font(.custom("ReemKufi-Regular", size: 15, relativeTo: .body))
                .foregroundStyle(.appPrimary)
                .padding(.top, 20)

            if let translation = dhikr.translation {
                translated(translation.sentenceCased, size: 15, color: .textPrimary)
                    .padding(.top, 10)
            }
        }
        .textSelection(.enabled)
    }

    /// Each phrase of a set with its count; the one to say now is highlighted and the finished ones fade
    private var steps: some View {
        let current = viewModel.currentStep?.index ?? 0

        return VStack(spacing: 10) {
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
                    Text(step.transliteration)
                        .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .body))
                        .foregroundStyle(.appPrimary)
                    if let translation = step.translation {
                        translated(translation.sentenceCased, size: 13, color: .textSecondary)
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(index == current ? Color.tintedSurface : .clear, in: .rect(cornerRadius: 14))
                .opacity(index < current ? 0.45 : 1)
                .id(index)
                .accessibilityElement(children: .combine)
            }
        }
        .padding(.top, 16)
        .animation(.snappy, value: current)
    }

    /// The hadith that gives the dhikr's virtue, in its published translation
    private func hadithCard(_ hadith: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Hadith")
                .font(.labelMedium)
                .foregroundStyle(.brandTeal)
            translated(hadith, size: 15, color: .textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.tintedSurface, in: .rect(cornerRadius: 16))
    }

    /// Translation text in the font and direction of its script, so Urdu is set in Nastaliq and right to left
    private func translated(_ text: String, size: CGFloat, color: Color) -> some View {
        let style = TranslationStyle(for: text, size: size)
        return Text(text)
            .font(style.font)
            .foregroundStyle(color)
            .lineSpacing(style.isRightToLeft ? 8 : 4)
            .multilineTextAlignment(style.isRightToLeft ? .trailing : .leading)
            .frame(maxWidth: .infinity, alignment: style.isRightToLeft ? .trailing : .leading)
    }

    private var source: some View {
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

    // MARK: - Counter

    private var counter: some View {
        VStack(spacing: 10) {
            if let step = viewModel.currentStep {
                let phrase = dhikr.steps[step.index]
                Text("\(phrase.transliteration) · \(step.said) of \(phrase.count)")
                    .font(.labelLarge)
                    .foregroundStyle(.appPrimary)
                    .contentTransition(.numericText())
            }

            Button {
                withAnimation(.snappy(duration: 0.2)) { viewModel.increment() }
            } label: {
                ZStack {
                    Circle()
                        .fill(.tintedSurface)
                    Circle()
                        .stroke(Color.brandTeal.opacity(0.2), lineWidth: 8)
                    Circle()
                        .trim(from: 0, to: viewModel.roundProgress)
                        .stroke(Color.brandTeal, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                        .rotationEffect(.degrees(-90))

                    VStack(spacing: 0) {
                        Text("\(viewModel.count)")
                            .font(.custom("ReemKufi-Medium", size: 46, relativeTo: .largeTitle))
                            .foregroundStyle(.appPrimary)
                            .contentTransition(.numericText())
                        Text(counterCaption)
                            .font(.labelSmall)
                            .foregroundStyle(.textSecondary)
                    }
                }
                .frame(width: counterSize, height: counterSize)
                .contentShape(.circle)
            }
            .buttonStyle(CounterButtonStyle())
            .sensoryFeedback(trigger: viewModel.count) { _, count in
                guard count > 0 else { return nil }
                switch viewModel.milestone(at: count) {
                case .repetition: return .impact(weight: .light)
                case .phraseFinished: return .impact(weight: .heavy)
                case .roundFinished: return .success
                }
            }
            .accessibilityLabel("Count")
            .accessibilityValue(counterAccessibilityValue)
            .accessibilityHint("Double-tap to count one")

            Text(viewModel.rounds == 1 ? "Completed once today" : "Completed \(viewModel.rounds) times today")
                .font(.labelSmall)
                .foregroundStyle(.brandTeal)
                .opacity(viewModel.rounds > 0 ? 1 : 0)
                .accessibilityHidden(viewModel.rounds == 0)
        }
        .padding(.top, 14)
        .padding(.bottom, 6)
        .frame(maxWidth: .infinity)
        .background(Color(.systemBackground).ignoresSafeArea(edges: .bottom))
        // Fades the text out just above the counter instead of cutting it off
        .overlay(alignment: .top) {
            LinearGradient(colors: [Color(.systemBackground).opacity(0), Color(.systemBackground)], startPoint: .top, endPoint: .bottom)
                .frame(height: 24)
                .offset(y: -24)
                .allowsHitTesting(false)
        }
    }

    private var counterCaption: String {
        guard let target = viewModel.target else {
            return viewModel.count == 0 ? "Tap to count" : "times"
        }
        return viewModel.isRoundComplete ? "Complete" : "of \(target)"
    }

    private var counterAccessibilityValue: String {
        guard let target = viewModel.target else { return "\(viewModel.count)" }
        return "\(viewModel.count) of \(target)"
    }
}

/// Presses in slightly, like a physical counter
private struct CounterButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
            .animation(.snappy(duration: 0.15), value: configuration.isPressed)
    }
}
