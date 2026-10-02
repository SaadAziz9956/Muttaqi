import Shared
import SwiftUI

struct TopicPageView: View {
    let title: String
    @State private var screen: SharedViewModel<TopicPageViewModel, TopicPageState>
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    init(emotionId: String) {
        self.init(title: "Emotions", chips: .emotions, topicId: emotionId)
    }

    init(exploreTopicId: String) {
        self.init(title: "Explore", chips: .exploreGroup, topicId: exploreTopicId)
    }

    private init(title: String, chips: TopicChips, topicId: String) {
        self.title = title
        _screen = State(initialValue: SharedViewModel(TopicsViewModels.shared.topicPage(chips: chips, topicId: topicId)) { $0.state })
    }

    private var state: TopicPageState { screen.state }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                topicTabs

                if state.sections.count > 1 {
                    Picker("Show", selection: Binding(get: { screen.viewModel.state.value.section }, set: { dispatch(TopicPageIntentSectionTapped(section: $0)) })) {
                        ForEach(state.sections, id: \.self) { section in
                            Text(section.title).tag(section)
                        }
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 20)
                    .padding(.top, 6)
                }

                if !state.topics.isEmpty {
                    content
                        .padding(.horizontal, 20)
                        .padding(.top, 20)
                }
            }
            .padding(.bottom, 32)
            .animation(.snappy, value: state.selectedId)
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
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openShare(let share): router.push(share.passage)
                }
            }
        }
    }

    private func dispatch(_ intent: TopicPageIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private var topicTabs: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                GlassEffectContainer(spacing: 4) {
                    HStack(spacing: 8) {
                        ForEach(state.topics, id: \.id) { topic in
                            let isSelected = topic.id == state.selectedId
                            Button {
                                dispatch(TopicPageIntentTopicTapped(topicId: topic.id))
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
                .padding(.vertical, 14)
            }
            .scrollIndicators(.hidden)
            .scrollClipDisabled()
            .onChange(of: state.topics.isEmpty, initial: true) { _, isEmpty in
                if !isEmpty { proxy.scrollTo(state.selectedId, anchor: .center) }
            }
            .onChange(of: state.selectedId) { _, id in
                withAnimation(.snappy) { proxy.scrollTo(id, anchor: .center) }
            }
        }
    }

    private var content: some View {
        VStack(spacing: 16) {
            ForEach(state.passages, id: \.id) { passage in
                card(for: passage)
            }

            if !state.translationCredits.isEmpty {
                Text("Translation: \(state.translationCredits.joined(separator: ", "))")
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
        .id(state.selectedId + state.section.title)
        .transition(.opacity)
    }

    private func card(for passage: TopicPassage) -> some View {
        let style = TranslationStyle(for: passage.translation, size: 14)

        return VStack(spacing: 12) {
            if !passage.arabic.isEmpty {
                Text(AttributedString.arabic(passage.arabic, size: 20))
                    .foregroundStyle(.textPrimary)
                    .lineSpacing(10)
            }
            Text(passage.translation)
                .font(style.font)
                .foregroundStyle(.textPrimary)
                .lineSpacing(style.isRightToLeft ? 8 : 4)
            HStack(spacing: 4) {
                if TranslationStyle.isArabicScript(passage.source) {
                    Text(passage.source)
                        .font(TranslationStyle(for: passage.source, size: 11).font)
                        .foregroundStyle(.brandTeal)
                        .lineSpacing(6)
                } else {
                    Text(passage.source)
                        .font(.labelSmall)
                        .foregroundStyle(.brandTeal)
                }
                Button {
                    dispatch(TopicPageIntentShareTapped(passageId: passage.id))
                } label: {
                    Image("export-arrow-01-linear")
                        .resizable()
                        .frame(width: 16, height: 16)
                        .foregroundStyle(.textSecondary)
                        .padding(6)
                        .contentShape(.rect)
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityLabel("Share")
            }
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 18)
        .padding(.vertical, 22)
        .softCard(cornerRadius: 26)
        .textSelection(.enabled)
    }
}
