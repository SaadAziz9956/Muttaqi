import Foundation

/// Use case for syncing Quran data with type-safe error handling
struct SyncQuranDataUseCase: Sendable {
    private let syncRepository: QuranSyncRepositoryProtocol
    private let preferences: ReadingPreferencesStore  // Use concrete type (class, reference)
    
    init(
        syncRepository: QuranSyncRepositoryProtocol,
        preferences: ReadingPreferencesStore
    ) {
        self.syncRepository = syncRepository
        self.preferences = preferences
    }
    
    /// Downloads Arabic + transliteration + specified language translation
    func execute(language: Language) async throws {
        do {
            // 1. Download Arabic text + transliteration
            try await syncRepository.syncArabicAndTransliteration()
            
            // 2. Download translation for selected language
            try await syncRepository.syncTranslation(language: language.code)
            
            // 3. Mark as downloaded (works because ReadingPreferencesStore is a class)
            preferences.isDataDownloaded = true
            preferences.selectedLanguage = language
            preferences.markLanguageAsDownloaded(language)
            
        } catch {
            throw SurahDetailError.failedToDownloadLanguage(
                language: language,
                underlyingError: error.localizedDescription
            )
        }
    }
}
