import Foundation

/// Dhikr.json: general remembrance from the Quran, Sahih al-Bukhari, Sahih Muslim and the Sunan, keeping only Quranic
/// text and hadith graded sahih or hasan. Every translation is taken word for word from a published one, named with
/// the entry: Saheeh International, Jalandhry and Junagarhi for the Quran; HadeethEnc, Hisn al-Muslim and the collections'
/// published translations for hadith.
final class BundledDhikrRepository: DhikrRepositoryProtocol {
    /// Published translations by language code, e.g. ["en": ..., "ur": ...]
    private typealias Translations = [String: String]

    private struct Book: Decodable {
        struct Section: Decodable {
            let id: String
            let title: String
            let subtitle: String
            let dhikr: [Entry]
        }
        struct Entry: Decodable {
            let id: String
            let title: String?
            let arabic: String
            let transliteration: String
            let translation: Translations?
            let steps: [Step]?
            let count: Int?
            let hadith: Translations?
            let reference: String
            let grade: String
            let credit: Translations?
        }
        struct Step: Decodable {
            let arabic: String
            let transliteration: String
            let translation: Translations
            let count: Int
        }
        let sections: [Section]
    }

    private let bundle: Bundle
    private var book: Book?

    init(bundle: Bundle = .main) {
        self.bundle = bundle
    }

    func sections(language: String) throws -> [DhikrSection] {
        try loadBook().sections.map { section in
            DhikrSection(
                id: section.id,
                title: section.title,
                subtitle: section.subtitle,
                dhikr: section.dhikr.map { entry in
                    Dhikr(
                        id: entry.id,
                        title: entry.title,
                        arabic: entry.arabic,
                        transliteration: entry.transliteration,
                        translation: entry.translation.flatMap { Self.pick($0, language) },
                        steps: (entry.steps ?? []).map { step in
                            Dhikr.Step(
                                arabic: step.arabic,
                                transliteration: step.transliteration,
                                translation: Self.pick(step.translation, language),
                                count: step.count
                            )
                        },
                        count: entry.count,
                        hadith: entry.hadith.flatMap { Self.pick($0, language) },
                        reference: entry.reference,
                        grade: entry.grade,
                        credit: entry.credit.flatMap { Self.pick($0, language) }
                    )
                }
            )
        }
    }

    private static func pick(_ translations: Translations, _ language: String) -> String? {
        translations[language] ?? translations["en"]
    }

    private func loadBook() throws -> Book {
        if let book { return book }
        guard let url = bundle.url(forResource: "Dhikr", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        let loaded = try JSONDecoder().decode(Book.self, from: Data(contentsOf: url))
        book = loaded
        return loaded
    }
}
