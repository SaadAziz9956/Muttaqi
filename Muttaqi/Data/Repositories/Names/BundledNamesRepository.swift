import Foundation

/// AsmaUlHusna.json: the 99 names as listed in Jami at-Tirmidhi 3507, with meanings taken word for word from published
/// translations: Darussalam's English and al-Faryiwa'i's Urdu
final class BundledNamesRepository: NamesRepositoryProtocol {
    /// Published translations by language code, e.g. ["en": ..., "ur": ...]
    private typealias Translations = [String: String]

    private struct Book: Decodable {
        struct Entry: Decodable {
            let number: Int
            let arabic: String
            let transliteration: String
            let meaning: Translations
        }
        let names: [Entry]
    }

    private let bundle: Bundle
    private var book: Book?

    init(bundle: Bundle = .main) {
        self.bundle = bundle
    }

    func names(language: String) throws -> [AllahName] {
        try loadBook().names.map { entry in
            AllahName(
                number: entry.number,
                arabic: entry.arabic,
                transliteration: entry.transliteration,
                meaning: entry.meaning[language] ?? entry.meaning["en"] ?? ""
            )
        }
    }

    private func loadBook() throws -> Book {
        if let book { return book }
        guard let url = bundle.url(forResource: "AsmaUlHusna", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        let loaded = try JSONDecoder().decode(Book.self, from: Data(contentsOf: url))
        book = loaded
        return loaded
    }
}
