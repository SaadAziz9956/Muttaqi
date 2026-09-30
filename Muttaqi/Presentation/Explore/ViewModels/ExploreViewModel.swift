import Foundation

@Observable
@MainActor
final class ExploreViewModel {
    struct SearchResult: Identifiable {
        let group: ExploreGroup
        let topic: ExploreTopic
        var id: String { topic.id }
    }

    private(set) var groups: [ExploreGroup] = []
    private(set) var header: PageQuote?
    var query = ""

    private let getExplore: GetExploreUseCase
    /// Folded text of each topic, built once per load for search: its names (title, keywords and group), and its
    /// verses', hadith's and duas' translations
    private var searchIndex: [(group: ExploreGroup, topic: ExploreTopic, names: String, text: String)] = []

    init(getExplore: GetExploreUseCase) {
        self.getExplore = getExplore
    }

    var isSearching: Bool {
        !query.trimmingCharacters(in: .whitespaces).isEmpty
    }

    /// Topics containing every word of the query: first those whose title, keywords or group match, e.g. "zakah" for
    /// Charity & Zakat, then those whose verses, hadith or duas mention it
    var searchResults: [SearchResult] {
        let words = query.searchFolded.split(separator: " ").map(String.init)
        guard !words.isEmpty else { return [] }
        let byName = searchIndex.filter { entry in words.allSatisfy { entry.names.contains($0) } }
        let byText = searchIndex.filter { entry in
            !words.allSatisfy { entry.names.contains($0) }
                && words.allSatisfy { entry.names.contains($0) || entry.text.contains($0) }
        }
        return (byName + byText).map { SearchResult(group: $0.group, topic: $0.topic) }
    }

    /// Reloads each time the page appears, so a translation language picked in the reader is used straight away
    func load() {
        guard let loaded = try? getExplore.execute() else { return }
        header = loaded.header
        groups = loaded.groups
        searchIndex = loaded.groups.flatMap { group in
            group.topics.map { topic in
                let names = ([topic.title, group.title] + topic.keywords).joined(separator: " ")
                let text = topic.verses.map(\.translation) + topic.hadith.map(\.translation)
                    + topic.duas.flatMap { [$0.translation, $0.transliteration] }
                return (group, topic, names.searchFolded, text.joined(separator: " ").searchFolded)
            }
        }
    }
}
