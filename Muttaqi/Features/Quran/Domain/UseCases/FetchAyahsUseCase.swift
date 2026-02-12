import Foundation

struct FetchAyahsUseCase: Sendable {
    private let repository: QuranRepositoryProtocol
    private let readingPreferences: ReadingPreferencesProtocol

    init(repository: QuranRepositoryProtocol, readingPreferences: ReadingPreferencesProtocol) {
        self.repository = repository
        self.readingPreferences = readingPreferences
    }

    func execute(surahNumber: Int) async throws -> [Ayah] {
        let language = readingPreferences.getSelectedLanguage()
        return try await repository.getAyahs(surahNumber: surahNumber, language: language)
    }
}
