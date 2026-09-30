import Foundation

struct GetAllahNamesUseCase: Sendable {
    private let repository: NamesRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: NamesRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute() throws -> [AllahName] {
        try repository.names(language: languagePreferences.getSelectedLanguage().code)
    }
}
