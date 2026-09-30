import SwiftUI

struct QuranListView: View {
    @State private var viewModel: QuranListViewModel
    @State private var titleBottom: CGFloat = .infinity
    @FocusState private var isSearchFocused: Bool
    @Environment(AppRouter.self) private var router

    init(viewModel: QuranListViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading:
                ProgressView()
                    .tint(.appPrimary)
            case .loaded:
                quranContent
            case .error(let message):
                errorView(message)
            }
        }
        .task {
            viewModel.send(.onAppear)
        }
        .onAppear {
            viewModel.onSurahSelected = { surah in
                router.push(AppRouter.QuranDestination.surahDetail(surah: surah))
            }
            viewModel.onContinueReading = { surah, ayah in
                router.push(AppRouter.QuranDestination.surahDetail(surah: surah, startAyah: ayah))
            }
        }
    }

    // MARK: - Main Content

    private var quranContent: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                // Custom title
                Text("The Quran")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .padding(.top, 24)

                headerSection
                if viewModel.readingProgress != nil {
                    continueReadingCard
                }
                searchField
                revelationChips
                    .padding(.top, 14)
                surahGrid
                    .padding(.top, 18)
            }
            .padding(.horizontal, 20)
        }
        .scrollDismissesKeyboard(.immediately)
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("The Quran", titleBottom: titleBottom)
    }

    // MARK: - Header

    private var headerSection: some View {
        let quote = PublishedQuote.learnAndTeachQuran.text(language: viewModel.language)

        return VStack(spacing: 0) {
            Text(quote.quoted)
                .font(TranslationStyle(for: quote, size: 14).font)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 8)

            Text(PublishedQuote.learnAndTeachQuran.source)
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .padding(.top, 4)
                .padding(.bottom, 24)
        }
    }

    // MARK: - Continue Reading Card

    // The whole card is one button, so it can be tapped anywhere and VoiceOver reads it as a single control
    @ViewBuilder
    private var continueReadingCard: some View {
        if let progress = viewModel.readingProgress {
            Button {
                viewModel.send(.continueTapped)
            } label: {
                VStack(alignment: .leading, spacing: 0) {
                    Text("Continue reading")
                        .font(.custom("ReemKufi-Regular", size: 13))
                        .foregroundStyle(.white.opacity(0.8))

                    HStack(alignment: .bottom) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(progress.surahEnglishName)
                                .font(.custom("ReemKufi-Medium", size: 26, relativeTo: .title))
                            Text("Ayah \(progress.lastAyahNumber)")
                                .font(.custom("ReemKufi-Regular", size: 13))
                                .foregroundStyle(.white.opacity(0.8))
                        }
                        Spacer(minLength: 8)
                        Text(progress.surahName)
                            .font(.arabic(30))
                    }
                    .padding(.top, 10)

                    Text("Continue")
                        .font(.custom("ReemKufi-Medium", size: 14))
                        .foregroundStyle(Color.shareCard)
                        .padding(.horizontal, 22)
                        .frame(height: 38)
                        .softGlass(in: Capsule(), fill: .white, rim: false)
                        .padding(.top, 16)
                }
                .foregroundStyle(.white)
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .softCard(rim: 3, artwork: .forest)
            }
            .buttonStyle(SoftPressStyle())
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Continue reading Surah \(progress.surahEnglishName), ayah \(progress.lastAyahNumber)")
            .accessibilityAddTraits(.isButton)
            .padding(.bottom, 16)
            .transition(.opacity)
        }
    }

    // MARK: - Search and filters

    private var searchField: some View {
        HStack(spacing: 10) {
            Image("search-normal-linear")
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.textSecondary)
                .accessibilityHidden(true)

            TextField("Search surah or number", text: $viewModel.query)
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

    private var revelationChips: some View {
        // Native glass rendered as one group, which is cheaper than each on its own
        GlassEffectContainer(spacing: 4) {
            HStack(spacing: 8) {
                ForEach(QuranListViewModel.Revelation.allCases) { place in
                    let isSelected = viewModel.revelation == place
                    Button {
                        withAnimation(.snappy) { viewModel.revelation = place }
                    } label: {
                        Text(place.rawValue)
                            .font(.custom("ReemKufi-Medium", size: 13))
                            .foregroundStyle(isSelected ? Color.white : Color.appPrimary)
                            .padding(.horizontal, 18)
                            .frame(height: 36)
                            .softGlass(in: Capsule(), fill: isSelected ? .shareCard : .softSurface, rim: !isSelected)
                    }
                    .buttonStyle(SoftPressStyle())
                    .accessibilityAddTraits(isSelected ? .isSelected : [])
                }
                Spacer(minLength: 0)
            }
        }
    }

    // MARK: - Surah Grid

    @ViewBuilder
    private var surahGrid: some View {
        let surahs = viewModel.visibleSurahs
        if surahs.isEmpty {
            ContentUnavailableView.search(text: viewModel.query)
                .padding(.top, 20)
        } else {
            let columns = [
                GridItem(.flexible(), spacing: 14),
                GridItem(.flexible(), spacing: 14)
            ]

            LazyVGrid(columns: columns, spacing: 14) {
                ForEach(surahs) { surah in
                    Button {
                        viewModel.send(.surahTapped(surah))
                    } label: {
                        SurahCardView(surah: surah)
                    }
                    .buttonStyle(SoftPressStyle())
                }
            }
            .padding(.bottom, 32)
        }
    }

    // MARK: - Error

    private func errorView(_ message: String) -> some View {
        VStack(spacing: 16) {
            Text("Failed to load")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)
            Text(message)
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
            Button {
                viewModel.send(.retry)
            } label: {
                Text("Retry")
                    .font(.titleSmall)
                    .foregroundStyle(.onPrimary)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding()
    }
}
