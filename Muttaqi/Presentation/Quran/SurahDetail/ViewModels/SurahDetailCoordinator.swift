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
    private(set) var tafsirViewModel: TafsirViewModel

    private let syncQuranData: SyncQuranDataUseCase
    private let fetchTafsirUseCase: FetchTafsirUseCase

    init(
        initialSurah: SurahNumber,
        headerSurah: Surah? = nil,
        fetchSurahs: FetchSurahsUseCase,
        fetchAyahs: FetchAyahsUseCase,
        syncQuranData: SyncQuranDataUseCase,
        readingPreferences: ReadingPreferences,
        fetchTafsir: FetchTafsirUseCase
    ) {
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
        }
    }

    // Fresh view model so the tafsir reloads for the current surah and language next time it opens
    private func resetTafsir() {
        tafsirViewModel = TafsirViewModel(fetchTafsir: fetchTafsirUseCase)
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
