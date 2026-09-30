import Foundation

struct SyncQuranDataUseCase: Sendable {
    private let syncRepository: QuranSyncRepositoryProtocol
    private let preferences: any ReadingPreferences
    
    init(
        syncRepository: QuranSyncRepositoryProtocol,
        preferences: any ReadingPreferences
    ) {
        self.syncRepository = syncRepository
        self.preferences = preferences
    }
    
    func execute(language: Language) async throws {
        do {
            try await syncRepository.syncArabicAndTransliteration()
            try await syncRepository.syncTranslation(language: language.code)
            
            preferences.setDataDownloaded(true)
            preferences.setSelectedLanguage(language)
            preferences.markLanguageAsDownloaded(language)
        } catch {
            throw SurahDetailError.failedToDownloadLanguage(
                language: language,
                underlyingError: error.localizedDescription
            )
        }
    }
}
