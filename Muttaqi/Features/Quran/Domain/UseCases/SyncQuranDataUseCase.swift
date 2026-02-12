import Foundation

struct SyncQuranDataUseCase: Sendable {
    private let syncRepository: QuranSyncRepositoryProtocol
    private let readingPreferences: ReadingPreferencesProtocol

    init(
        syncRepository: QuranSyncRepositoryProtocol,
        readingPreferences: ReadingPreferencesProtocol
    ) {
        self.syncRepository = syncRepository
        self.readingPreferences = readingPreferences
    }

    /// Downloads Arabic + transliteration + selected language translation
    func execute(language: String) async throws {
        // 1. Download Arabic text + transliteration
        try await syncRepository.syncArabicAndTransliteration()
        // 2. Download translation for selected language
        try await syncRepository.syncTranslation(language: language)
        // 3. Mark as downloaded
        readingPreferences.setDataDownloaded(true)
        readingPreferences.setSelectedLanguage(language)
        readingPreferences.addDownloadedLanguage(language)
    }
}
