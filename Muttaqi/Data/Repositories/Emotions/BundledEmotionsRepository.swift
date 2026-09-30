import Foundation

/// Emotions.json: the Quran verses, authentic hadith and Hisn al-Muslim duas chosen for each emotion, in the shape
/// described in `BundledPassages`
final class BundledEmotionsRepository: EmotionsRepositoryProtocol {
    private struct Book: Decodable {
        struct Entry: Decodable {
            let id: String
            let title: String
            let verses: [BundledPassages.Verse]
            let hadith: [BundledPassages.Hadith]
            let duas: [Int]
        }
        let header: BundledPassages.Quote
        let emotions: [Entry]
    }

    private let bundle: Bundle
    private let duaRepository: DuaRepositoryProtocol
    private var book: Book?

    init(duaRepository: DuaRepositoryProtocol, bundle: Bundle = .main) {
        self.duaRepository = duaRepository
        self.bundle = bundle
    }

    func header(language: String) throws -> PageQuote {
        BundledPassages.quote(try loadBook().header, language: language)
    }

    func emotions(language: String) throws -> [Emotion] {
        let duas = try BundledPassages.duas(from: duaRepository, language: language)
        return try loadBook().emotions.map { entry in
            Emotion(
                id: entry.id,
                title: entry.title,
                verses: entry.verses.map { BundledPassages.verse($0, language: language) },
                hadith: entry.hadith.map { BundledPassages.hadith($0, language: language) },
                duas: entry.duas.compactMap { duas["hisn-\($0)"] }
            )
        }
    }

    private func loadBook() throws -> Book {
        if let book { return book }
        let loaded = try BundledPassages.decode(Book.self, resource: "Emotions", bundle: bundle)
        book = loaded
        return loaded
    }
}
