import Foundation

struct GetDhikrSectionsUseCase: Sendable {
    private let repository: DhikrRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: DhikrRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute() throws -> [DhikrSection] {
        try repository.sections(language: languagePreferences.getSelectedLanguage().code)
    }
}
