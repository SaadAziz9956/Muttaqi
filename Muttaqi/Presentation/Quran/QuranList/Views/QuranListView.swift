import SwiftUI

struct QuranListView: View {
    @State private var viewModel: QuranListViewModel
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
            viewModel.onSurahSelected = { number in
                router.push(AppRouter.QuranDestination.surahDetail(number: number))
            }
            viewModel.onContinueReading = { surahNumber, _ in
                router.push(AppRouter.QuranDestination.surahDetail(number: surahNumber))
            }
        }
    }

    // MARK: - Main Content

    private var quranContent: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                // Custom title
                Text("The Quran")
                    .font(.custom("ReemKufi-Bold", size: 32))
                    .foregroundStyle(.appPrimary)
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
        .toolbar {
            ToolbarItem(placement: .principal) {
                EmptyView()
            }
        }
    }

    // MARK: - Header

    private var headerSection: some View {
        VStack(spacing: 0) {
            Text("The best among you [Muslims] are those who learn the Quran and teach it.")
                .font(.bodyMedium)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 8)

            Text("Sahih Bukhari (5027)")
                .font(.labelMedium)
                .foregroundStyle(.textSecondary)
                .padding(.top, 4)
                .padding(.bottom, 24)
        }
    }

    // MARK: - Continue Reading Card

    private var continueReadingCard: some View {
        Group {
            if let progress = viewModel.readingProgress {
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Surah \(progress.surahEnglishName)")
                                .font(.titleSmall)
                                .foregroundStyle(.appPrimary)
                            Text("Ayah: \(progress.lastAyahNumber)")
                                .font(.bodySmall)
                                .foregroundStyle(.textSecondary)
                        }
                        Spacer()
                        Button {
                            viewModel.send(.continueTapped)
                        } label: {
                            Text("Continue")
                                .font(.bodyMedium)
                                .foregroundStyle(.appPrimary)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(.appPrimary, lineWidth: 1)
                                )
                        }
                    }

                    HStack {
                        Text("Completed")
                            .font(.bodySmall)
                            .foregroundStyle(.textPrimary)
                        Spacer()
                        Text("\(Int(progress.progressPercentage)) %")
                            .font(.bodySmall)
                            .foregroundStyle(.textPrimary)
                    }
                    .padding(.top, 12)

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 4)
                                .fill(Color(.systemGray5))
                                .frame(height: 8)
                            RoundedRectangle(cornerRadius: 4)
                                .fill(.appPrimary)
                                .frame(width: geo.size.width * progress.progressPercentage / 100, height: 8)
                        }
                    }
                    .frame(height: 8)
                    .padding(.top, 8)
                }
                .padding(16)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.bottom, 16)
            }
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
                        viewModel.send(.surahTapped(surah.number))
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
                    .foregroundStyle(.white)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding()
    }
}
