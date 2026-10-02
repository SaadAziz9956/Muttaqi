import Shared
import SwiftUI

struct QuranListView: View {
    @State private var screen = SharedViewModel(QuranViewModels.shared.list()) { $0.state }
    @State private var titleBottom: CGFloat = .infinity
    @FocusState private var isSearchFocused: Bool
    @Environment(AppRouter.self) private var router

    private var state: QuranListState { screen.state }

    var body: some View {
        Group {
            if state.isLoading {
                ProgressView()
                    .tint(.appPrimary)
            } else if let error = state.error {
                errorView(error)
            } else {
                quranContent
            }
        }
        .task {
            dispatch(QuranListIntentAppeared.shared)
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openSurah(let open):
                    guard let surah = surah(open.surahNumber) else { continue }
                    router.push(AppRouter.QuranDestination.surahDetail(surah: surah))
                case .continueReading(let next):
                    guard let surah = surah(next.surahNumber) else { continue }
                    router.push(AppRouter.QuranDestination.surahDetail(surah: surah, startAyah: Int(next.ayahNumber)))
                }
            }
        }
    }

    private func dispatch(_ intent: QuranListIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private func surah(_ number: Int32) -> Surah? {
        state.surahs.first { $0.number == number }
    }

    private var quranContent: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                Text("The Quran")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .padding(.top, 24)

                headerSection
                if state.readingProgress != nil {
                    continueReadingCard
                }
                searchField
                revelationChips
                    .padding(.top, 14)
                surahGrid
                    .padding(.top, 18)
            }
            .padding(.horizontal, 20)
            .animation(.smooth, value: state.readingProgress)
            .animation(.snappy, value: state.filter)
        }
        .scrollDismissesKeyboard(.immediately)
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("The Quran", titleBottom: titleBottom)
    }

    @ViewBuilder
    private var headerSection: some View {
        if let quote = state.header {
            VStack(spacing: 0) {
                Text(quote.text.quoted)
                    .font(TranslationStyle(for: quote.text, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 8)

                Text(quote.source)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
                    .padding(.bottom, 24)
            }
        }
    }

    @ViewBuilder
    private var continueReadingCard: some View {
        if let progress = state.readingProgress {
            Button {
                dispatch(QuranListIntentContinueTapped.shared)
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

    private var searchField: some View {
        HStack(spacing: 10) {
            Image("search-normal-linear")
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.textSecondary)
                .accessibilityHidden(true)

            TextField(
                "Search surah or number",
                text: Binding(get: { state.query }, set: { dispatch(QuranListIntentQueryChanged(query: $0)) })
            )
            .font(.bodyMedium)
            .foregroundStyle(.textPrimary)
            .submitLabel(.search)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .focused($isSearchFocused)

            if state.isSearching {
                Button {
                    dispatch(QuranListIntentClearQuery.shared)
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
        GlassEffectContainer(spacing: 4) {
            HStack(spacing: 8) {
                ForEach(RevelationFilter.allCases, id: \.self) { filter in
                    let isSelected = state.filter == filter
                    Button {
                        dispatch(QuranListIntentFilterSelected(filter: filter))
                    } label: {
                        Text(filter.label)
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

    @ViewBuilder
    private var surahGrid: some View {
        let surahs = state.visibleSurahs
        if surahs.isEmpty {
            ContentUnavailableView.search(text: state.query)
                .padding(.top, 20)
        } else {
            let columns = [
                GridItem(.flexible(), spacing: 14),
                GridItem(.flexible(), spacing: 14)
            ]

            LazyVGrid(columns: columns, spacing: 14) {
                ForEach(surahs, id: \.number) { surah in
                    Button {
                        dispatch(QuranListIntentSurahTapped(surahNumber: surah.number))
                    } label: {
                        SurahCardView(surah: surah)
                    }
                    .buttonStyle(SoftPressStyle())
                }
            }
            .padding(.bottom, 32)
        }
    }

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
                dispatch(QuranListIntentRetry.shared)
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
