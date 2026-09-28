import Foundation

@Observable
@MainActor
final class SurahDetailCoordinator {
    let navigator: SurahNavigator
    let contentViewModel: SurahContentViewModel
    let settingsViewModel: ReadingSettingsViewModel

    var showSettings = false
    var showTafsir = false
    private(set) var headerSurah: Surah?
    private(set) var previousSurah: Surah?
    private(set) var nextSurah: Surah?
    private(set) var tafsirViewModel: TafsirViewModel
    /// Ayah (number within the surah) to scroll to once the surah loads; cleared after the view has scrolled there
    private(set) var pendingStartAyah: Int?

    private let syncQuranData: SyncQuranDataUseCase
    private let fetchTafsirUseCase: FetchTafsirUseCase
    private let updateReadingProgress: UpdateReadingProgressUseCase
    private var pendingProgress: (surah: Int, lastAyah: Int, readAyahs: Set<Int>, totalAyahs: Int)?
    /// Ayahs of the current surah that have been on screen since it was opened
    private var sessionReadAyahs: Set<Int> = []
    /// The scroll-target IDs on screen now, kept even while jumping so they can be counted once it lands
    private var currentVisibleIDs: [Int] = []
    private var saveProgressTask: Task<Void, Never>?

    init(
        initialSurah: SurahNumber,
        headerSurah: Surah? = nil,
        startAyah: Int? = nil,
        fetchSurahs: FetchSurahsUseCase,
        fetchAyahs: FetchAyahsUseCase,
        syncQuranData: SyncQuranDataUseCase,
        readingPreferences: ReadingPreferences,
        fetchTafsir: FetchTafsirUseCase,
        updateReadingProgress: UpdateReadingProgressUseCase
    ) {
        self.pendingStartAyah = startAyah
        self.updateReadingProgress = updateReadingProgress
        self.navigator = SurahNavigator(surah: initialSurah)
        self.contentViewModel = SurahContentViewModel(
            fetchSurahs: fetchSurahs,
            fetchAyahs: fetchAyahs
        )
        self.settingsViewModel = ReadingSettingsViewModel(preferences: readingPreferences)
        self.syncQuranData = syncQuranData
        self.fetchTafsirUseCase = fetchTafsir
        self.headerSurah = headerSurah
        self.tafsirViewModel = TafsirViewModel(fetchTafsir: fetchTafsir)
    }

    func onAppear() async {
        guard contentViewModel.state == .idle else { return }
        await contentViewModel.loadSurah(navigator.currentSurah)
        updateHeaderSurah()
    }

    // Synchronous: updates navigator state only. Call inside withAnimation {}.
    @discardableResult
    func goNext() -> Bool {
        guard navigator.canGoNext else { return false }
        navigator.navigateNext()
        return true
    }

    @discardableResult
    func goPrevious() -> Bool {
        guard navigator.canGoPrevious else { return false }
        navigator.navigatePrevious()
        return true
    }

    // Async: loads content for the current surah after navigation.
    func loadCurrentSurah() async {
        saveProgressNow()
        sessionReadAyahs = []
        currentVisibleIDs = []
        pendingStartAyah = nil
        await contentViewModel.loadSurah(navigator.currentSurah)
        updateHeaderSurah()
        resetTafsir()
    }

    func retry() async {
        await contentViewModel.retry(surahNumber: navigator.currentSurah)
        updateHeaderSurah()
    }

    private func updateHeaderSurah() {
        if case .loaded(let content) = contentViewModel.state {
            headerSurah = content.surah
            previousSurah = content.previousSurah
            nextSurah = content.nextSurah
        }
    }

    // Fresh view model so the tafsir reloads for the current surah and language next time it opens
    private func resetTafsir() {
        tafsirViewModel = TafsirViewModel(fetchTafsir: fetchTafsirUseCase)
    }

    // MARK: - Reading progress

    func didScrollToStartAyah() {
        pendingStartAyah = nil
        // The screen the jump lands on only reports a visibility change once the reader scrolls, so count it now
        recordVisible(currentVisibleIDs)
    }

    /// Records what's on screen. `visibleIDs` are the scroll-target IDs: an ayah's `id` in translation mode,
    /// and the `id` of each Mushaf page's first ayah in Arabic Only mode.
    func recordVisible(_ visibleIDs: [Int]) {
        currentVisibleIDs = visibleIDs
        // Ignore what scrolls past while jumping to the saved position, or it would overwrite that position
        guard pendingStartAyah == nil, case .loaded(let content) = contentViewModel.state else { return }
        let visible = content.displayAyahs.filter { visibleIDs.contains($0.id) }
        guard let first = visible.min(by: { $0.numberInSurah < $1.numberInSurah }) else { return }

        // Every ayah on screen counts as read; in Arabic Only mode a visible page shows all of its ayahs
        var onScreen: Set<Int>
        switch settingsViewModel.readingMode {
        case .withTranslation:
            onScreen = Set(visible.map(\.numberInSurah))
        case .arabicOnly:
            let visiblePages = Set(visible.map(\.page))
            onScreen = Set(content.displayAyahs.filter { visiblePages.contains($0.page) }.map(\.numberInSurah))
        }
        // Al-Fatiha's first ayah is the Bismillah, shown above the list rather than as its own card
        if content.surah.number == 1, first.numberInSurah <= 3 {
            onScreen.insert(1)
        }
        sessionReadAyahs.formUnion(onScreen)

        pendingProgress = (content.surah.number, first.numberInSurah, sessionReadAyahs, content.surah.numberOfAyahs)
        // Saved once scrolling settles rather than on every frame
        saveProgressTask?.cancel()
        saveProgressTask = Task { [weak self] in
            try? await Task.sleep(for: .seconds(1))
            guard !Task.isCancelled else { return }
            self?.saveProgressNow()
        }
    }

    /// Writes any unsaved position immediately, e.g. when the reader leaves the screen
    func saveProgressNow() {
        saveProgressTask?.cancel()
        guard let progress = pendingProgress else { return }
        pendingProgress = nil
        let useCase = updateReadingProgress
        Task {
            try? await useCase.execute(
                surahNumber: progress.surah,
                lastAyahNumber: progress.lastAyah,
                readAyahs: progress.readAyahs,
                totalAyahs: progress.totalAyahs
            )
        }
    }

    func toggleSettings() {
        showSettings.toggle()
    }

    func toggleTafsir() {
        showTafsir.toggle()
    }

    func selectLanguage(_ language: Language) async {
        do {
            try await settingsViewModel.selectLanguage(language) { [weak self] lang in
                guard let self else { return }
                try await self.syncQuranData.execute(language: lang)
            }
            // Reload content with new language
            await contentViewModel.loadSurah(navigator.currentSurah)
            resetTafsir()
        } catch {
            // Handle error (could add error state to settings view model)
            print("Failed to download language: \(error)")
        }
    }
}
