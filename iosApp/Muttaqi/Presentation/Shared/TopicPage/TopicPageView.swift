import SwiftUI

/// One topic's Quran verses, hadith and duas, with tabs to move to its neighbours, e.g. an emotion or an Explore topic
struct TopicPageView<Topic: PassageTopic>: View {
    /// Shown in the bar, e.g. "Emotions"
    let title: String
    @State private var viewModel: TopicPageViewModel<Topic>
    @Environment(\.dismiss) private var dismiss

    init(title: String, viewModel: TopicPageViewModel<Topic>) {
        self.title = title
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                topicTabs

                if viewModel.sections.count > 1 {
                    Picker("Show", selection: $viewModel.section) {
                        ForEach(viewModel.sections) { section in
                            Text(section.rawValue).tag(section)
                        }
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 20)
                    .padding(.top, 6)
                }

                if let topic = viewModel.selected {
                    content(for: topic)
                        .padding(.horizontal, 20)
                        .padding(.top, 20)
                }
            }
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
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
                Text(title)
                    .font(.custom("ReemKufi-Regular", size: 20))
                    .foregroundStyle(.appPrimary)
            }
        }
        .onAppear { viewModel.load() }
    }

    // MARK: - Tabs

    private var topicTabs: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                // Native glass rendered as one group, which is cheaper than each on its own
                GlassEffectContainer(spacing: 4) {
                    HStack(spacing: 8) {
                        ForEach(viewModel.topics) { topic in
                            let isSelected = topic.id == viewModel.selected?.id
                            Button {
                                withAnimation(.snappy) { viewModel.select(topic) }
                            } label: {
                                Text(topic.title)
                                    .font(.custom("ReemKufi-Medium", size: 13, relativeTo: .subheadline))
                                    .foregroundStyle(isSelected ? Color.white : Color.appPrimary)
                                    .fixedSize()
                                    .padding(.horizontal, 18)
                                    .frame(minHeight: 36)
                                    .softGlass(in: Capsule(), fill: isSelected ? .shareCard : .softSurface, rim: !isSelected)
                            }
                            .buttonStyle(SoftPressStyle())
                            .id(topic.id)
                            .accessibilityAddTraits(isSelected ? .isSelected : [])
                        }
                    }
                }
                .padding(.horizontal, 20)
                // Room for the chips' float shadow, which a scroll view would otherwise clip
                .padding(.vertical, 14)
            }
            .scrollIndicators(.hidden)
            .scrollClipDisabled()
            // Once the topics have loaded, so a topic far along the row starts in view
            .onChange(of: viewModel.topics.isEmpty, initial: true) { _, isEmpty in
                if !isEmpty { proxy.scrollTo(viewModel.selectedID, anchor: .center) }
            }
            .onChange(of: viewModel.selectedID) { _, id in
                withAnimation(.snappy) { proxy.scrollTo(id, anchor: .center) }
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func content(for topic: Topic) -> some View {
        VStack(spacing: 16) {
            switch viewModel.section {
            case .quran:
                ForEach(topic.verses, id: \.reference) { verse in
                    passage(arabic: verse.arabic, translation: verse.translation, source: "Quran (\(verse.reference))",
                            share: SharePassage(verse: verse))
                }
            case .hadith:
                ForEach(topic.hadith, id: \.self) { hadith in
                    passage(arabic: hadith.arabic, translation: hadith.translation, source: "\(hadith.attribution) · \(hadith.grade)",
                            share: SharePassage(hadith: hadith))
                }
            case .dua:
                ForEach(topic.duas) { dua in
                    passage(arabic: dua.arabic, translation: dua.translation, source: dua.source, share: SharePassage(dua: dua))
                }
            }

            // Names whose translations are shown, as their publishers ask
            if let credits = credits(for: topic) {
                Text("Translation: \(credits)")
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
        .id(topic.id + viewModel.section.rawValue)
        .transition(.opacity)
    }

    private func passage(arabic: String, translation: String, source: String, share: SharePassage) -> some View {
        let style = TranslationStyle(for: translation, size: 14)

        return VStack(spacing: 12) {
            if !arabic.isEmpty {
                Text(AttributedString.arabic(arabic, size: 20))
                    .foregroundStyle(.textPrimary)
                    .lineSpacing(10)
            }
            Text(translation)
                .font(style.font)
                .foregroundStyle(.textPrimary)
                .lineSpacing(style.isRightToLeft ? 8 : 4)
            HStack(spacing: 4) {
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
                ShareButton(passage: share, size: 16)
            }
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 18)
        .padding(.vertical, 22)
        .softCard(cornerRadius: 26)
        .textSelection(.enabled)
    }

    private func credits(for topic: Topic) -> String? {
        let names: [String] = switch viewModel.section {
        case .quran: topic.verses.map(\.credit)
        case .hadith: topic.hadith.map(\.credit)
        case .dua: topic.duas.map(\.translationCredit)
        }
        let distinct = names.reduce(into: [String]()) { if !$0.contains($1) { $0.append($1) } }
        return distinct.isEmpty ? nil : distinct.joined(separator: ", ")
    }
}
