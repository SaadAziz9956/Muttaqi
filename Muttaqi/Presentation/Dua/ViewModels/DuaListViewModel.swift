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
        let words = Self.fold(query).split(separator: " ").map(String.init)
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
                return (category, chapter, Self.fold(parts.joined(separator: " ")))
            }
        }
    }

    // Case- and accent-insensitive. Foundation's diacritic folding leaves Arabic harakat in place, so those are
    // stripped here, along with tatweel, and letter variants are unified, so Arabic matches with or without vowels
    private static func fold(_ text: String) -> String {
        let folded = text.folding(options: [.caseInsensitive, .diacriticInsensitive, .widthInsensitive], locale: nil)
        let scalars = folded.unicodeScalars.compactMap { scalar -> Unicode.Scalar? in
            switch scalar.value {
            case 0x064B...0x065F, 0x0670, 0x06D6...0x06ED, 0x0640: return nil
            case 0x0622, 0x0623, 0x0625, 0x0671: return "\u{0627}" // آ أ إ ٱ → ا
            case 0x0629: return "\u{0647}"                          // ة → ه
            case 0x0649: return "\u{064A}"                          // ى → ي
            default: return scalar
            }
        }
        return String(String.UnicodeScalarView(scalars))
    }
}
