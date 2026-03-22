import Foundation

@Observable
@MainActor
final class ReadingSettingsViewModel {
    private let preferences: any ReadingPreferences
    
    private(set) var readingMode: ReadingMode
    private(set) var fontSize: FontSize
    private(set) var selectedLanguage: Language
    private(set) var isDownloadingLanguage = false
    
    var downloadedLanguages: [Language] {
        preferences.getDownloadedLanguages()
    }
    
    init(preferences: any ReadingPreferences) {
        self.preferences = preferences
        self.readingMode = preferences.getReadingMode()
        self.fontSize = preferences.getFontSize()
        self.selectedLanguage = preferences.getSelectedLanguage()
    }
    
    func setReadingMode(_ mode: ReadingMode) {
        readingMode = mode
        preferences.setReadingMode(mode)
    }
    
    func increaseFontSize() {
        fontSize = fontSize.increased()
        preferences.setFontSize(fontSize)
    }
    
    func decreaseFontSize() {
        fontSize = fontSize.decreased()
        preferences.setFontSize(fontSize)
    }
    
    func selectLanguage(_ language: Language, downloader: @escaping (Language) async throws -> Void) async throws {
        guard language != selectedLanguage else { return }
        
        if downloadedLanguages.contains(language) {
            selectedLanguage = language
            preferences.setSelectedLanguage(language)
        } else {
            isDownloadingLanguage = true
            do {
                try await downloader(language)
                selectedLanguage = language
                preferences.setSelectedLanguage(language)
                preferences.markLanguageAsDownloaded(language)
                isDownloadingLanguage = false
            } catch {
                isDownloadingLanguage = false
                throw error
            }
        }
    }
}
