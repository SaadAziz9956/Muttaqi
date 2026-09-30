import Foundation

@Observable
@MainActor
final class DuaListViewModel {
    struct SearchResult: Identifiable {
        let category: DuaCategory
        let chapter: DuaChapter
        var id: String { chapter.id }
    }

    private(set) var categories: [DuaCategory] = []
    private(set) var quote: DailyAyah?
    var query = ""

    private let getCategories: GetDuaCategoriesUseCase
    private let fetchAyah: FetchAyahUseCase
    /// Folded, lower-cased text of each chapter (titles plus every entry), built once for search
    private var searchIndex: [(category: DuaCategory, chapter: DuaChapter, text: String)] = []

    init(getCategories: GetDuaCategoriesUseCase, fetchAyah: FetchAyahUseCase) {
        self.getCategories = getCategories
        self.fetchAyah = fetchAyah
    }

    var isSearching: Bool {
        !query.trimmingCharacters(in: .whitespaces).isEmpty
    }

    /// Chapters containing every word of the query, in the title or any of its duas, in English or Arabic
    var searchResults: [SearchResult] {
        let words = query.searchFolded.split(separator: " ").map(String.init)
        guard !words.isEmpty else { return [] }
        return searchIndex
            .filter { entry in words.allSatisfy { entry.text.contains($0) } }
            .map { SearchResult(category: $0.category, chapter: $0.chapter) }
    }

    func load() async {
        // 40:60 "Call upon Me; I will respond to you", shown under the title
        quote = try? await fetchAyah.execute(surahNumber: 40, ayahNumber: 60)
        guard let loaded = try? getCategories.execute() else { return }
        categories = loaded
        searchIndex = loaded.flatMap { category in
            category.chapters.map { chapter in
                let parts = [category.title, chapter.title, chapter.titleArabic ?? ""]
                    + chapter.entries.flatMap { [$0.translation, $0.transliteration, $0.arabic, $0.source] }
                return (category, chapter, parts.joined(separator: " ").searchFolded)
            }
        }
    }
}
