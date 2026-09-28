import Foundation

struct GetDuaCategoriesUseCase: Sendable {
    private let repository: DuaRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: DuaRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute() throws -> [DuaCategory] {
        try repository.categories(language: languagePreferences.getSelectedLanguage().code)
    }
}
