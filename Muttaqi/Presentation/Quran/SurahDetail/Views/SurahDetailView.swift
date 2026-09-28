import SwiftUI

struct SurahDetailView: View {
    @State private var coordinator: SurahDetailCoordinator
    @State private var contentWidth: CGFloat = 0
    @State private var titleBottom: CGFloat = .infinity
    @Environment(\.dismiss) private var dismiss

    init(coordinator: SurahDetailCoordinator) {
        self._coordinator = State(initialValue: coordinator)
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 0) {
                        SurahHeaderView(
                            surah: coordinator.headerSurah,
                            previousSurah: coordinator.previousSurah,
                            nextSurah: coordinator.nextSurah,
                            onPrevious: goToPreviousSurah,
                            onNext: goToNextSurah,
                            onExplanation: {
                                coordinator.toggleTafsir()
                            },
                            onTitleBottomChange: { titleBottom = $0 }
                        )

                        contentView
                    }
                    .scrollTargetLayout()
                    .padding(.horizontal, 16)
                }
                // Ayahs (or Mushaf pages) at least a fifth on screen count as read
                .onScrollTargetVisibilityChange(idType: Int.self, threshold: 0.2) { visibleIDs in
                    coordinator.recordVisible(visibleIDs)
                }
                .task(id: startScrollTarget) {
                    guard let target = startScrollTarget else { return }
                    // Ayah cards are laid out lazily, so the first jump uses estimated heights and can land an ayah
                    // off; once the cards around the target are laid out, a second jump lands exactly
                    proxy.scrollTo(target, anchor: .top)
                    try? await Task.sleep(for: .milliseconds(300))
                    proxy.scrollTo(target, anchor: .top)
                    try? await Task.sleep(for: .milliseconds(300))
                    coordinator.didScrollToStartAyah()
                }
            }
            .id(coordinator.navigator.currentSurah.value)
            .transition(SurahSlideTransition(navigator: coordinator.navigator, width: contentWidth))
        }
        .onDisappear {
            coordinator.saveProgressNow()
        }
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { contentWidth = $0 }
        .collapsingBarTitle(coordinator.headerSurah?.englishName ?? "", titleBottom: titleBottom)
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
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { coordinator.toggleSettings() } label: {
                    Image("setting-4-linear")
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
        .sheet(isPresented: $coordinator.showTafsir) {
            TafsirView(
                surah: coordinator.headerSurah,
                viewModel: coordinator.tafsirViewModel,
                language: coordinator.settingsViewModel.selectedLanguage
            )
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
        }
        .task {
            await coordinator.onAppear()
        }
    }

    /// Scroll ID for the ayah the reader is continuing from: the ayah itself, or its Mushaf page in Arabic Only mode
    private var startScrollTarget: Int? {
        guard let startAyah = coordinator.pendingStartAyah,
              case .loaded(let content) = coordinator.contentViewModel.state,
              let ayah = content.displayAyahs.first(where: { $0.numberInSurah == startAyah }) else { return nil }
        switch coordinator.settingsViewModel.readingMode {
        case .withTranslation:
            return ayah.id
        case .arabicOnly:
            return content.displayAyahs.first(where: { $0.page == ayah.page })?.id
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
                    fontSize: coordinator.settingsViewModel.fontSize,
                    language: coordinator.settingsViewModel.selectedLanguage
                )
            }
        case .arabicOnly:
            ArabicOnlyView(
                ayahs: content.displayAyahs,
                fontSize: coordinator.settingsViewModel.fontSize
            )
            .padding(.top, 16)
        }

        SurahEndNavigationView(
            previousSurah: content.previousSurah,
            nextSurah: content.nextSurah,
            onPrevious: goToPreviousSurah,
            onNext: goToNextSurah
        )
        .padding(.top, 24)
        .padding(.bottom, 32)
    }

    private func goToPreviousSurah() {
        let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goPrevious() }
        if moved { Task { await coordinator.loadCurrentSurah() } }
    }

    private func goToNextSurah() {
        let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goNext() }
        if moved { Task { await coordinator.loadCurrentSurah() } }
    }

    private var content: SurahContentViewModel.SurahContent? {
        if case .loaded(let content) = coordinator.contentViewModel.state {
            return content
        }
        return nil
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
                    .foregroundStyle(.onPrimary)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.top, 100)
    }
}

// Slides the surah in from, and out toward, the side of the arrow that was tapped.
// The direction is read from the navigator as the transition runs rather than captured: SwiftUI animates an outgoing
// view with the transition from its last render, which still holds the old direction when the user switches arrows.
private struct SurahSlideTransition: Transition {
    let navigator: SurahNavigator
    let width: CGFloat

    func body(content: Content, phase: TransitionPhase) -> some View {
        // Forward: the new surah enters from the trailing edge and the old one leaves by the leading edge
        let sign: CGFloat = navigator.navigationDirection == .forward ? 1 : -1
        content.offset(x: -phase.value * sign * width)
    }
}
