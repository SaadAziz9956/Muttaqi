import SwiftUI

/// One emotion's Quran verses, hadith and duas, with tabs to move to another emotion
struct EmotionDetailView: View {
    @State private var viewModel: EmotionsViewModel
    @Environment(\.dismiss) private var dismiss

    init(viewModel: EmotionsViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                emotionTabs

                if viewModel.sections.count > 1 {
                    Picker("Show", selection: $viewModel.section) {
                        ForEach(viewModel.sections) { section in
                            Text(section.rawValue).tag(section)
                        }
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                }

                if let emotion = viewModel.selected {
                    content(for: emotion)
                        .padding(.horizontal, 20)
                        .padding(.top, 32)
                }
            }
            .padding(.bottom, 32)
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
            ToolbarItem(placement: .principal) {
                Text("Emotions")
                    .font(.custom("ReemKufi-Regular", size: 20))
                    .foregroundStyle(.appPrimary)
            }
        }
        .onAppear { viewModel.load() }
    }

    // MARK: - Tabs

    private var emotionTabs: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                HStack(alignment: .firstTextBaseline, spacing: 24) {
                    ForEach(viewModel.emotions) { emotion in
                        let isSelected = emotion.id == viewModel.selected?.id
                        Button {
                            withAnimation(.snappy) { viewModel.select(emotion) }
                        } label: {
                            VStack(spacing: 6) {
                                Text(emotion.title)
                                    .font(.custom("ReemKufi-Regular", size: isSelected ? 16 : 12, relativeTo: .subheadline))
                                    .foregroundStyle(isSelected ? Color.appPrimary : Color.textSecondary)
                                Capsule()
                                    .fill(isSelected ? Color.appPrimary : .clear)
                                    .frame(height: 2)
                            }
                            .fixedSize()
                            .contentShape(.rect)
                        }
                        .buttonStyle(.plain)
                        .id(emotion.id)
                        .accessibilityAddTraits(isSelected ? .isSelected : [])
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
            }
            .scrollIndicators(.hidden)
            .onAppear { proxy.scrollTo(viewModel.selectedID, anchor: .center) }
            .onChange(of: viewModel.selectedID) { _, id in
                withAnimation(.snappy) { proxy.scrollTo(id, anchor: .center) }
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func content(for emotion: Emotion) -> some View {
        VStack(spacing: 40) {
            switch viewModel.section {
            case .quran:
                ForEach(emotion.verses, id: \.reference) { verse in
                    passage(arabic: verse.arabic, translation: verse.translation, source: "Quran (\(verse.reference))")
                }
            case .hadith:
                ForEach(emotion.hadith, id: \.self) { hadith in
                    passage(arabic: nil, translation: hadith.translation, source: "\(hadith.attribution) · \(hadith.grade)")
                }
            case .dua:
                ForEach(emotion.duas) { dua in
                    passage(arabic: dua.arabic, translation: dua.translation, source: dua.source)
                }
            }

            // Names whose translations are shown, as their publishers ask
            if let credits = credits(for: emotion) {
                Text("Translation: \(credits)")
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
        .id(emotion.id + viewModel.section.rawValue)
        .transition(.opacity)
    }

    private func passage(arabic: String?, translation: String, source: String) -> some View {
        let style = TranslationStyle(for: translation, size: 14)

        return VStack(spacing: 12) {
            if let arabic {
                Text(AttributedString.arabic(arabic, size: 20))
                    .foregroundStyle(.textPrimary)
                    .lineSpacing(10)
            }
            Text(translation)
                .font(style.font)
                .foregroundStyle(.textPrimary)
                .lineSpacing(style.isRightToLeft ? 8 : 4)
            // An Urdu attribution, e.g. "اسے امام بخاری نے روایت کیا ہے", is set in Nastaliq like its translation
            if TranslationStyle.isArabicScript(source) {
                Text(source)
                    .font(TranslationStyle(for: source, size: 11).font)
                    .foregroundStyle(.brandTeal)
                    .lineSpacing(6)
            } else {
                Text(source)
                    .font(.labelSmall)
                    .foregroundStyle(.brandTeal)
            }
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .textSelection(.enabled)
    }

    private func credits(for emotion: Emotion) -> String? {
        let names: [String] = switch viewModel.section {
        case .quran: emotion.verses.map(\.credit)
        case .hadith: emotion.hadith.map(\.credit)
        case .dua: emotion.duas.map(\.translationCredit)
        }
        let distinct = names.reduce(into: [String]()) { if !$0.contains($1) { $0.append($1) } }
        return distinct.isEmpty ? nil : distinct.joined(separator: ", ")
    }
}
