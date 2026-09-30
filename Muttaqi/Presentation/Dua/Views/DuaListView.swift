import SwiftUI

struct DuaListView: View {
    @State private var viewModel: DuaListViewModel
    @State private var titleBottom: CGFloat = .infinity
    @FocusState private var isSearchFocused: Bool
    @Environment(AppRouter.self) private var router

    init(viewModel: DuaListViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                header
                    .padding(.top, 24)

                searchField
                    .padding(.top, 36)

                if viewModel.isSearching {
                    searchResults
                        .padding(.top, 20)
                } else {
                    categoryGrid
                        .padding(.top, 20)
                }
            }
            .padding(.horizontal, 22)
            .padding(.bottom, 32)
        }
        .scrollDismissesKeyboard(.immediately)
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Dua", titleBottom: titleBottom)
        .task { await viewModel.load() }
    }

    private var header: some View {
        VStack(spacing: 0) {
            Text("Dua")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }

            if let quote = viewModel.quote, let translation = quote.ayah.translation {
                Text(translation.quoted)
                    .font(TranslationStyle(for: translation, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text("Quran (\(quote.reference))")
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

            TextField("Search", text: $viewModel.query)
                .font(.bodyMedium)
                .foregroundStyle(.textPrimary)
                .submitLabel(.search)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .focused($isSearchFocused)

            if viewModel.isSearching {
                Button {
                    viewModel.query = ""
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

    private var categoryGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)], spacing: 14) {
            ForEach(viewModel.categories) { category in
                Button {
                    open(category)
                } label: {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(category.title)
                            .font(.custom("ReemKufi-Medium", size: 15))
                            .foregroundStyle(.appPrimary)
                            .multilineTextAlignment(.leading)
                            .lineLimit(2)
                            .minimumScaleFactor(0.85)
                        Spacer(minLength: 0)
                        Text(category.entryCount == 1 ? "1 dua" : "\(category.entryCount) duas")
                            .font(.custom("ReemKufi-Regular", size: 12))
                            .foregroundStyle(.brandTeal)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, minHeight: 96, alignment: .leading)
                    .softCard(cornerRadius: 24)
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityHint("\(category.entryCount) duas")
            }
        }
    }

    @ViewBuilder
    private var searchResults: some View {
        let results = viewModel.searchResults
        if results.isEmpty {
            ContentUnavailableView.search(text: viewModel.query)
                .padding(.top, 20)
        } else {
            LazyVStack(spacing: 0) {
                ForEach(results) { result in
                    Button {
                        router.pushDua(.chapter(result.chapter))
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(result.chapter.title)
                                    .font(.bodyMedium)
                                    .foregroundStyle(.textPrimary)
                                    .multilineTextAlignment(.leading)
                                Text(result.category.title)
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

    // A category with a single chapter opens straight to its duas
    private func open(_ category: DuaCategory) {
        if category.chapters.count == 1, let chapter = category.chapters.first {
            router.pushDua(.chapter(chapter))
        } else {
            router.pushDua(.category(category))
        }
    }
}
