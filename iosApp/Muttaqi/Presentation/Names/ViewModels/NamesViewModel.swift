import Foundation

/// Shared by the 99 Names page and its search, so picking a search result turns the page to that name
@Observable
@MainActor
final class NamesViewModel {
    enum SearchMode: Hashable {
        case number, name
    }

    private(set) var names: [AllahName] = []
    /// The reader's translation language, for the page's hadith
    private(set) var language = "en"
    /// The name on screen, as its number; bound to the swiper's scroll position
    var currentNumber: Int? = 1
    var query = ""
    var searchMode: SearchMode = .number

    private let getNames: GetAllahNamesUseCase
    private let languagePreferences: LanguagePreferences

    init(getNames: GetAllahNamesUseCase, languagePreferences: LanguagePreferences) {
        self.getNames = getNames
        self.languagePreferences = languagePreferences
    }

    func load() {
        language = languagePreferences.getSelectedLanguage().code
        names = (try? getNames.execute()) ?? []
    }

    var isSearching: Bool {
        !query.trimmingCharacters(in: .whitespaces).isEmpty
    }

    /// By number: the name with that number. By name: names whose transliteration or meaning contains the query,
    /// ignoring case, hyphens and doubled vowels, so "rahman" finds "Ar-Rahmaan"
    var results: [AllahName] {
        let trimmed = query.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return [] }
        switch searchMode {
        case .number:
            guard let number = Int(trimmed) else { return [] }
            return names.filter { $0.number == number }
        case .name:
            let folded = Self.fold(trimmed)
            guard !folded.isEmpty else { return [] }
            return names.filter { Self.fold($0.transliteration).contains(folded) || Self.fold($0.meaning).contains(folded) }
        }
    }

    private static func fold(_ text: String) -> String {
        var folded = text.folding(options: [.caseInsensitive, .diacriticInsensitive], locale: nil)
            .filter { $0.isLetter }
        for (long, short) in [("aa", "a"), ("ee", "i"), ("ii", "i"), ("oo", "u"), ("uu", "u")] {
            folded = folded.replacingOccurrences(of: long, with: short)
        }
        return folded
    }
}
