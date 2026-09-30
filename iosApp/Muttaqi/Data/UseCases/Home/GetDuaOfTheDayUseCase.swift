import Foundation

/// Picks the same dua all day, moving to the next one at midnight
struct GetDuaOfTheDayUseCase: Sendable {
    private let repository: DuaRepositoryProtocol
    private let languagePreferences: LanguagePreferences
    private let calendar: Calendar

    init(repository: DuaRepositoryProtocol, languagePreferences: LanguagePreferences, calendar: Calendar = .current) {
        self.repository = repository
        self.languagePreferences = languagePreferences
        self.calendar = calendar
    }

    func execute(on date: Date = .now) throws -> Dua? {
        let duas = try repository.duas(language: languagePreferences.getSelectedLanguage().code)
        guard !duas.isEmpty else { return nil }
        let day = calendar.ordinality(of: .day, in: .era, for: date) ?? 0
        return duas[day % duas.count]
    }
}
