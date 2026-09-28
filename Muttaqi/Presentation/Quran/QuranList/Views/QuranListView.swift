import SwiftUI

struct QuranListView: View {
    @State private var viewModel: QuranListViewModel
    @State private var titleBottom: CGFloat = .infinity
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
                surahGrid
            }
            .padding(.horizontal, 16)
        }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("The Quran", titleBottom: titleBottom)
    }

    // MARK: - Header

    private var headerSection: some View {
        VStack(spacing: 0) {
            Text("“The best among you [Muslims] are those who learn the Quran and teach it.”")
                .font(.custom("ReemKufi-Regular", size: 14))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 8)

            Text("Sahih Bukhari (5027)")
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
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Surah \(progress.surahEnglishName)")
                            .font(.bodyLarge)
                            .foregroundStyle(.brandTeal)
                        Text("Ayah: \(progress.lastAyahNumber)")
                            .font(.labelSmall)
                            .foregroundStyle(.textSecondary)
                    }

                    Spacer()

                    Text("Continue")
                        .font(.bodySmall)
                        .foregroundStyle(.brandTeal)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                        .background(Color(.systemBackground), in: .rect(cornerRadius: 8))
                }
                .padding(20)
                .background(Color.brandTeal.opacity(0.12), in: .rect(cornerRadius: 16))
            }
            .buttonStyle(.plain)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Continue reading Surah \(progress.surahEnglishName), ayah \(progress.lastAyahNumber)")
            .accessibilityAddTraits(.isButton)
            .padding(.bottom, 24)
            .transition(.opacity)
        }
    }

    // MARK: - Surah Grid

    private var surahGrid: some View {
        let columns = [
            GridItem(.flexible(), spacing: 12),
            GridItem(.flexible(), spacing: 12)
        ]

        return LazyVGrid(columns: columns, spacing: 12) {
            ForEach(viewModel.surahs) { surah in
                SurahCardView(surah: surah)
                    .onTapGesture {
                        viewModel.send(.surahTapped(surah))
                    }
            }
        }
        .padding(.bottom, 24)
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
