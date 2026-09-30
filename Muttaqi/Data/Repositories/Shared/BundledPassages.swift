import Foundation

/// The JSON shape Emotions.json and Explore.json share: Quran verses (Uthmani Arabic, Saheeh International,
/// Jalandhry), authentic hadith (HadeethEnc's published English and Urdu, in full, with its Arabic) and Hisn al-Muslim
/// duas by their number in the book. Translations are keyed by language code, e.g. ["en": ..., "ur": ...].
enum BundledPassages {
    typealias Translations = [String: String]

    struct Quote: Decodable {
        let reference: String
        let translation: Translations
    }

    struct Verse: Decodable {
        let reference: String
        let arabic: String
        let translation: Translations
    }

    struct Hadith: Decodable {
        let arabic: String
        let translation: Translations
        let attribution: Translations
        let grade: Translations
        let source: String
    }

    static func quote(_ quote: Quote, language: String) -> PageQuote {
        PageQuote(reference: quote.reference, translation: pick(quote.translation, language))
    }

    static func verse(_ verse: Verse, language: String) -> QuranPassage {
        QuranPassage(
            reference: verse.reference,
            arabic: verse.arabic,
            translation: pick(verse.translation, language),
            credit: verse.translation[language] != nil && language == "ur"
                ? "Fateh Muhammad Jalandhry"
                : "Saheeh International"
        )
    }

    static func hadith(_ hadith: Hadith, language: String) -> HadithPassage {
        HadithPassage(
            arabic: hadith.arabic,
            translation: pick(hadith.translation, language),
            attribution: pick(hadith.attribution, language),
            grade: pick(hadith.grade, language),
            credit: hadith.source
        )
    }

    /// Hisn al-Muslim entries by their id, e.g. "hisn-176", already in the reader's language
    static func duas(from repository: DuaRepositoryProtocol, language: String) throws -> [String: DuaEntry] {
        try repository.categories(language: language)
            .flatMap { $0.chapters.flatMap(\.entries) }
            .reduce(into: [String: DuaEntry]()) { $0[$1.id] = $1 }
    }

    static func decode<Book: Decodable>(_ type: Book.Type, resource: String, bundle: Bundle) throws -> Book {
        guard let url = bundle.url(forResource: resource, withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        return try JSONDecoder().decode(Book.self, from: Data(contentsOf: url))
    }

    private static func pick(_ translations: Translations, _ language: String) -> String {
        translations[language] ?? translations["en"] ?? ""
    }
}
