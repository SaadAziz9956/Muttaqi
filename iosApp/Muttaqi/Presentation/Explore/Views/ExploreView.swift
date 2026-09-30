import Shared
import SwiftUI

/// Topics in groups, e.g. Worship › Fasting; each opens its Quran verses, hadith and duas
struct ExploreView: View {
    @State private var screen = SharedViewModel(TopicsViewModels.shared.explore()) { $0.state }
    @State private var titleBottom: CGFloat = .infinity
    @FocusState private var isSearchFocused: Bool
    @Environment(AppRouter.self) private var router

    private var state: ExploreState { screen.state }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                header
                    .padding(.top, 24)

                searchField
                    .padding(.top, 36)

                if state.isSearching {
                    searchResults
                        .padding(.top, 20)
                } else {
                    topicGroups
                        .padding(.top, 8)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .scrollDismissesKeyboard(.immediately)
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Explore", titleBottom: titleBottom)
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openTopic(let open): router.pushExplore(.topic(id: open.topicId))
                }
            }
        }
    }

    private func dispatch(_ intent: ExploreIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private var header: some View {
        VStack(spacing: 0) {
            Text("Explore")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }

            if let quote = state.header {
                Text(quote.text.quoted)
                    .font(TranslationStyle(for: quote.text, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text(quote.source)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
            }
        }
    }

    private var searchField: some View {
        HStack(spacing: 10) {
            Image("search-normal-linear")
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.textSecondary)
                .accessibilityHidden(true)

            // Reads the view model's query as it is now rather than the last one drawn: clearing unfocuses the field in
            // the same moment, and a field still showing the old text would send it back as it lets go
            TextField("Search", text: Binding(get: { screen.viewModel.state.value.query }, set: { dispatch(ExploreIntentQueryChanged(query: $0)) }))
                .font(.bodyMedium)
                .foregroundStyle(.textPrimary)
                .submitLabel(.search)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .focused($isSearchFocused)

            if state.isSearching {
                Button {
                    dispatch(ExploreIntentClearQuery.shared)
                    isSearchFocused = false
                } label: {
                    Image("close-circle-bold")
                        .resizable()
                        .frame(width: 18, height: 18)
                        .foregroundStyle(.textSecondary)
                }
                .accessibilityLabel("Clear search")
            }
        }
        .padding(.horizontal, 18)
        .frame(height: 50)
        .softGlass(in: Capsule())
        .onTapGesture { isSearchFocused = true }
    }

    private var topicGroups: some View {
        LazyVStack(alignment: .leading, spacing: 0) {
            ForEach(state.groups, id: \.id) { group in
                Text(group.title)
                    .font(.titleSmall)
                    .foregroundStyle(.appPrimary)
                    .accessibilityAddTraits(.isHeader)
                    .padding(.top, 28)
                    .padding(.bottom, 12)

                LazyVGrid(columns: [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)], spacing: 14) {
                    ForEach(group.topics, id: \.id) { topic in
                        Button {
                            dispatch(ExploreIntentTopicTapped(topicId: topic.id))
                        } label: {
                            TopicTile(topic: topic)
                        }
                        .buttonStyle(SoftPressStyle())
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var searchResults: some View {
        let results = state.results
        if results.isEmpty {
            ContentUnavailableView.search(text: state.query)
                .padding(.top, 20)
        } else {
            LazyVStack(spacing: 0) {
                ForEach(results, id: \.topic.id) { result in
                    Button {
                        dispatch(ExploreIntentTopicTapped(topicId: result.topic.id))
                    } label: {
                        HStack(spacing: 14) {
                            Image(result.topic.icon)
                                .resizable()
                                .frame(width: 22, height: 22)
                                .foregroundStyle(.brandTeal)
                                .accessibilityHidden(true)
                            VStack(alignment: .leading, spacing: 3) {
                                Text(result.topic.title)
                                    .font(.bodyMedium)
                                    .foregroundStyle(.textPrimary)
                                    .multilineTextAlignment(.leading)
                                Text(result.group.title)
                                    .font(.labelSmall)
                                    .foregroundStyle(.brandTeal)
                            }
                            Spacer(minLength: 8)
                            Image("arrow-right-02-linear")
                                .resizable()
                                .frame(width: 16, height: 16)
                                .foregroundStyle(.textSecondary)
                        }
                        .padding(16)
                        .softCard(cornerRadius: 22)
                    }
                    .buttonStyle(SoftPressStyle())
                    .padding(.bottom, 12)
                }
            }
        }
    }
}

private struct TopicTile: View {
    let topic: ExploreTopic

    var body: some View {
        HStack(spacing: 10) {
            Image(topic.icon)
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.brandTeal)
                .padding(8)
                .background(.tintedSurface, in: .circle)
                .accessibilityHidden(true)
            Text(topic.title)
                .font(.custom("ReemKufi-Medium", size: 13, relativeTo: .footnote))
                .foregroundStyle(.appPrimary)
                .multilineTextAlignment(.leading)
                .lineLimit(2)
                // Room for the ﷺ after the Prophet's name, which is wide in any font
                .minimumScaleFactor(0.7)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 12)
        .frame(maxWidth: .infinity, minHeight: 60)
        .softCard(cornerRadius: 22)
    }
}
