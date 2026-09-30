import Foundation

struct GetEmotionsUseCase: Sendable {
    private let repository: EmotionsRepositoryProtocol
    private let languagePreferences: LanguagePreferences

    init(repository: EmotionsRepositoryProtocol, languagePreferences: LanguagePreferences) {
        self.repository = repository
        self.languagePreferences = languagePreferences
    }

    func execute() throws -> (header: PageQuote, emotions: [Emotion]) {
        let language = languagePreferences.getSelectedLanguage().code
        return (try repository.header(language: language), try repository.emotions(language: language))
    }
}
