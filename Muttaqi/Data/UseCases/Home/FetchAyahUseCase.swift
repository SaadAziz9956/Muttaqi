import Foundation

/// Fetches one ayah, with its surah, in the reader's translation language
struct FetchAyahUseCase: Sendable {
    private let repository: QuranRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: QuranRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute(surahNumber: Int, ayahNumber: Int) async throws -> DailyAyah? {
        let language = languagePreferences.getSelectedLanguage().code
        guard let surah = try await repository.getSurah(number: surahNumber),
              let ayah = try await repository.getAyah(surahNumber: surahNumber, numberInSurah: ayahNumber, language: language)
        else { return nil }
        return DailyAyah(surah: surah, ayah: ayah)
    }
}
