import Foundation

/// Use case for fetching Ayahs with type-safe value objects
struct FetchAyahsUseCase: Sendable {
    private let repository: QuranRepositoryProtocol
    private let languagePreferences: LanguagePreferences
    
    init(
        repository: QuranRepositoryProtocol,
        languagePreferences: LanguagePreferences
    ) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }
    
    func execute(surahNumber: SurahNumber) async throws -> [Ayah] {
        let language = languagePreferences.getSelectedLanguage()
        return try await repository.getAyahs(
            surahNumber: surahNumber.value,
            language: language.code
        )
    }
}
