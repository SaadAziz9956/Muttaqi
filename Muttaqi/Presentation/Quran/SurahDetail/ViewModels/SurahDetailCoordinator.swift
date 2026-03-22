import Foundation

@Observable
@MainActor
final class SurahDetailCoordinator {
    let navigator: SurahNavigator
    let contentViewModel: SurahContentViewModel
    let settingsViewModel: ReadingSettingsViewModel
    
    var showSettings = false
    
    private let syncQuranData: SyncQuranDataUseCase
    
    init(
        initialSurah: SurahNumber,
        fetchSurahs: FetchSurahsUseCase,
        fetchAyahs: FetchAyahsUseCase,
        syncQuranData: SyncQuranDataUseCase,
        readingPreferences: ReadingPreferences
    ) {
        self.navigator = SurahNavigator(surah: initialSurah)
        self.contentViewModel = SurahContentViewModel(
            fetchSurahs: fetchSurahs,
            fetchAyahs: fetchAyahs
        )
        self.settingsViewModel = ReadingSettingsViewModel(preferences: readingPreferences)
        self.syncQuranData = syncQuranData
    }
    
    func onAppear() async {
        guard contentViewModel.state == .idle else { return }
        await contentViewModel.loadSurah(navigator.currentSurah)
    }
    
    func navigateNext() async {
        guard navigator.canGoNext else { return }
        navigator.navigateNext()
        await contentViewModel.loadSurah(navigator.currentSurah)
    }
    
    func navigatePrevious() async {
        guard navigator.canGoPrevious else { return }
        navigator.navigatePrevious()
        await contentViewModel.loadSurah(navigator.currentSurah)
    }
    
    func retry() async {
        await contentViewModel.retry(surahNumber: navigator.currentSurah)
    }
    
    func toggleSettings() {
        showSettings.toggle()
    }
    
    func selectLanguage(_ language: Language) async {
        do {
            try await settingsViewModel.selectLanguage(language) { [weak self] lang in
                guard let self else { return }
                try await self.syncQuranData.execute(language: lang)
            }
            // Reload content with new language
            await contentViewModel.loadSurah(navigator.currentSurah)
        } catch {
            // Handle error (could add error state to settings view model)
            print("Failed to download language: \(error)")
        }
    }
}
