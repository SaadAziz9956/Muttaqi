import Foundation

/// Explore.json: topics in groups, each with the Quran verses, authentic hadith and Hisn al-Muslim duas chosen for
/// it, in the shape described in `BundledPassages`
final class BundledExploreRepository: ExploreRepositoryProtocol {
    private struct Book: Decodable {
        struct Group: Decodable {
            let id: String
            let title: String
            let topics: [Topic]
        }
        struct Topic: Decodable {
            let id: String
            let title: String
            let icon: String
            let keywords: [String]
            let verses: [BundledPassages.Verse]
            let hadith: [BundledPassages.Hadith]
            let duas: [Int]
        }
        let header: BundledPassages.Quote
        let groups: [Group]
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

    func groups(language: String) throws -> [ExploreGroup] {
        let duas = try BundledPassages.duas(from: duaRepository, language: language)
        return try loadBook().groups.map { group in
            ExploreGroup(
                id: group.id,
                title: group.title,
                topics: group.topics.map { topic in
                    ExploreTopic(
                        id: topic.id,
                        title: topic.title,
                        icon: topic.icon,
                        keywords: topic.keywords,
                        verses: topic.verses.map { BundledPassages.verse($0, language: language) },
                        hadith: topic.hadith.map { BundledPassages.hadith($0, language: language) },
                        duas: topic.duas.compactMap { duas["hisn-\($0)"] }
                    )
                }
            )
        }
    }

    private func loadBook() throws -> Book {
        if let book { return book }
        let loaded = try BundledPassages.decode(Book.self, resource: "Explore", bundle: bundle)
        book = loaded
        return loaded
    }
}
