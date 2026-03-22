import SwiftUI

struct SurahDetailView: View {
    @State private var coordinator: SurahDetailCoordinator

    init(coordinator: SurahDetailCoordinator) {
        self._coordinator = State(initialValue: coordinator)
    }

    var body: some View {
        VStack(spacing: 0) {
            SurahHeaderView(
                content: content,
                canGoNext: coordinator.navigator.canGoNext,
                canGoPrevious: coordinator.navigator.canGoPrevious,
                onPrevious: { Task { await coordinator.navigatePrevious() } },
                onNext: { Task { await coordinator.navigateNext() } }
            )

            ScrollView {
                LazyVStack(spacing: 0) {
                    contentView
                }
                .padding(.horizontal, 16)
            }
            .id(coordinator.navigator.currentSurah.value)
            .transition(slideTransition)
            .clipped()
        }
        .animation(.easeInOut(duration: 0.3), value: coordinator.navigator.currentSurah)
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .tabBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { coordinator.toggleSettings() } label: {
                    Image("ic_settings")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
            }
        }
        .sheet(isPresented: $coordinator.showSettings) {
            ReadingSettingsSheet(
                settingsViewModel: coordinator.settingsViewModel,
                onLanguageSelected: { language in
                    Task { await coordinator.selectLanguage(language) }
                }
            )
            .presentationDetents([.medium])
            .presentationDragIndicator(.visible)
        }
        .task {
            await coordinator.onAppear()
        }
    }

    @ViewBuilder
    private var contentView: some View {
        switch coordinator.contentViewModel.state {
        case .idle:
            EmptyView()
        case .loading:
            ProgressView()
                .tint(.appPrimary)
                .padding(.top, 100)
        case .loaded(let content):
            loadedContent(content)
        case .error(let error):
            errorView(error)
        }
    }

    @ViewBuilder
    private func loadedContent(_ content: SurahContentViewModel.SurahContent) -> some View {
        if content.showBismillah {
            BismillahView(
                text: content.bismillahText,
                translation: content.bismillahTranslation
            )
        }

        switch coordinator.settingsViewModel.readingMode {
        case .withTranslation:
            ForEach(content.displayAyahs) { ayah in
                AyahCardView(
                    ayah: ayah,
                    fontSize: coordinator.settingsViewModel.fontSize.value
                )
            }
        case .arabicOnly:
            ArabicOnlyView(
                ayahs: content.displayAyahs,
                fontSize: coordinator.settingsViewModel.fontSize.value
            )
            .padding(.top, 16)
            .padding(.bottom, 32)
        }
    }

    private var content: SurahContentViewModel.SurahContent? {
        if case .loaded(let content) = coordinator.contentViewModel.state {
            return content
        }
        return nil
    }

    private var slideTransition: AnyTransition {
        switch coordinator.navigator.navigationDirection {
        case .forward:
            return .asymmetric(
                insertion: .move(edge: .trailing),
                removal: .move(edge: .leading)
            )
        case .backward:
            return .asymmetric(
                insertion: .move(edge: .leading),
                removal: .move(edge: .trailing)
            )
        }
    }

    private func errorView(_ error: SurahDetailError) -> some View {
        VStack(spacing: 16) {
            Text("Failed to load")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)
            Text(error.localizedDescription)
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            if let suggestion = error.recoverySuggestion {
                Text(suggestion)
                    .font(.caption)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            Button {
                Task { await coordinator.retry() }
            } label: {
                Text("Retry")
                    .font(.titleSmall)
                    .foregroundStyle(.white)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.top, 100)
    }
}

