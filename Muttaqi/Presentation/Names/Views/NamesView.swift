import SwiftUI

struct NamesView: View {
    @State private var viewModel: NamesViewModel
    @State private var isSearchOpen = false
    @Environment(\.dismiss) private var dismiss

    init(viewModel: NamesViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        let hadith = HadithQuote.ninetyNineNames.text(language: viewModel.language)

        GeometryReader { screen in
            ScrollView {
                VStack(spacing: 0) {
                    Text("99 Names")
                        .font(.custom("ReemKufi-Regular", size: 28))
                        .foregroundStyle(.appPrimary)
                        .padding(.top, 8)

                    // 300pt on an iPhone Pro, in proportion on bigger and smaller screens
                    carousel(cardHeight: max(260, screen.size.height * 0.415))
                        .padding(.top, 36)

                    // The hadith sits at the foot of the screen, as in the design; the page only scrolls when a
                    // long card leaves no room for it there
                    Spacer(minLength: 32)

                    VStack(spacing: 6) {
                        Text(hadith)
                            .font(TranslationStyle(for: hadith, size: 14).font)
                            .foregroundStyle(.textSecondary)
                        Text(HadithQuote.ninetyNineNames.source)
                            .font(.labelSmall)
                            .foregroundStyle(.brandTeal)
                    }
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
                    .padding(.bottom, 12)
                }
                .frame(minHeight: screen.size.height)
            }
            .scrollBounceBehavior(.basedOnSize)
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
                Button { isSearchOpen = true } label: {
                    Image("search-normal-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
                .accessibilityLabel("Search names")
            }
        }
        .navigationDestination(isPresented: $isSearchOpen) {
            NamesSearchView(viewModel: viewModel) { name in
                // Picking a result turns the page to that name
                viewModel.currentNumber = name.number
                isSearchOpen = false
            }
        }
        .onAppear {
            if viewModel.names.isEmpty { viewModel.load() }
        }
    }

    /// One name at a time, with the next and previous peeking in at the edges, smaller and faded. As a card is
    /// swiped in it grows to full size while the one leaving shrinks back.
    private func carousel(cardHeight: CGFloat) -> some View {
        ScrollView(.horizontal) {
            LazyHStack(alignment: .top, spacing: 12) {
                ForEach(viewModel.names) { name in
                    NameCard(name: name, minHeight: cardHeight)
                        .padding(.vertical, 6)
                        .containerRelativeFrame(.horizontal)
                        .scrollTransition(axis: .horizontal) { card, phase in
                            card
                                // Shrinks toward the middle card, so the peeking edge stays in view
                                .scaleEffect(phase.isIdentity ? 1 : 0.88, anchor: phase.value < 0 ? .trailing : .leading)
                                .opacity(phase.isIdentity ? 1 : 0.6)
                        }
                }
            }
            .scrollTargetLayout()
        }
        .contentMargins(.horizontal, 38, for: .scrollContent)
        .scrollTargetBehavior(.viewAligned)
        .scrollPosition(id: $viewModel.currentNumber)
        .scrollIndicators(.hidden)
        .sensoryFeedback(.selection, trigger: viewModel.currentNumber)
    }
}
