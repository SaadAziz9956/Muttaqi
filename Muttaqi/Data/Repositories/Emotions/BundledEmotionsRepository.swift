import Foundation

/// Emotions.json: Quran verses (Uthmani Arabic, Saheeh International, Jalandhry), authentic hadith (HadeethEnc's
/// published English and Urdu, in full) and Hisn al-Muslim duas chosen for each emotion. Duas are listed by their
/// number in the book and taken from the Hisn al-Muslim bundled for the Dua tab.
final class BundledEmotionsRepository: EmotionsRepositoryProtocol {
    /// Published translations by language code, e.g. ["en": ..., "ur": ...]
    private typealias Translations = [String: String]

    private struct Book: Decodable {
        struct Header: Decodable {
            let reference: String
            let translation: Translations
        }
        struct Entry: Decodable {
            let id: String
            let title: String
            let verses: [Verse]
            let hadith: [Hadith]
            let duas: [Int]
        }
        struct Verse: Decodable {
            let reference: String
            let arabic: String
            let translation: Translations
        }
        struct Hadith: Decodable {
            let translation: Translations
            let attribution: Translations
            let grade: Translations
            let source: String
        }
        let header: Header
        let emotions: [Entry]
    }

    private let bundle: Bundle
    private let duaRepository: DuaRepositoryProtocol
    private var book: Book?

    init(duaRepository: DuaRepositoryProtocol, bundle: Bundle = .main) {
        self.duaRepository = duaRepository
        self.bundle = bundle
    }

    func header(language: String) throws -> EmotionsHeader {
        let header = try loadBook().header
        return EmotionsHeader(reference: header.reference, translation: Self.pick(header.translation, language))
    }

    func emotions(language: String) throws -> [Emotion] {
        // Hisn al-Muslim entries by their number in the book, already in the reader's language
        let duas = try duaRepository.categories(language: language)
            .flatMap { $0.chapters.flatMap(\.entries) }
            .reduce(into: [String: DuaEntry]()) { $0[$1.id] = $1 }

        return try loadBook().emotions.map { entry in
            Emotion(
                id: entry.id,
                title: entry.title,
                verses: entry.verses.map { verse in
                    EmotionVerse(
                        reference: verse.reference,
                        arabic: verse.arabic,
                        translation: Self.pick(verse.translation, language),
                        credit: verse.translation[language] != nil && language == "ur"
                            ? "Fateh Muhammad Jalandhry"
                            : "Saheeh International"
                    )
                },
                hadith: entry.hadith.map { hadith in
                    EmotionHadith(
                        translation: Self.pick(hadith.translation, language),
                        attribution: Self.pick(hadith.attribution, language),
                        grade: Self.pick(hadith.grade, language),
                        credit: hadith.source
                    )
                },
                duas: entry.duas.compactMap { duas["hisn-\($0)"] }
            )
        }
    }

    private static func pick(_ translations: Translations, _ language: String) -> String {
        translations[language] ?? translations["en"] ?? ""
    }

    private func loadBook() throws -> Book {
        if let book { return book }
        guard let url = bundle.url(forResource: "Emotions", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        let loaded = try JSONDecoder().decode(Book.self, from: Data(contentsOf: url))
        book = loaded
        return loaded
    }
}
