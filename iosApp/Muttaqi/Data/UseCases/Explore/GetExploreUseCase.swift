import Foundation

struct GetExploreUseCase: Sendable {
    private let repository: ExploreRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: ExploreRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute() throws -> (header: PageQuote, groups: [ExploreGroup]) {
        let language = languagePreferences.getSelectedLanguage().code
        return (try repository.header(language: language), try repository.groups(language: language))
    }
}
