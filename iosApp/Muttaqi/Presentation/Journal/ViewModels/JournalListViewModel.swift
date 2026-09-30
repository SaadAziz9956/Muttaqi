import Foundation

@Observable
@MainActor
final class JournalListViewModel {
    var query = ""
    /// 68:1 "Nun. By the pen and what they inscribe", shown under the title
    private(set) var quote: DailyAyah?

    private let store: JournalStore
    private let fetchAyah: FetchAyahUseCase

    init(store: JournalStore, fetchAyah: FetchAyahUseCase) {
        self.store = store
        self.fetchAyah = fetchAyah
    }

    var hasLoaded: Bool { store.hasLoaded }
    var hasEntries: Bool { !store.entries.isEmpty }

    var isSearching: Bool {
        !query.trimmingCharacters(in: .whitespaces).isEmpty
    }

    /// Every entry, or while searching, those whose title or body contains every word of the query
    var entries: [JournalEntry] {
        let words = query.split(separator: " ").map(String.init)
        guard !words.isEmpty else { return store.entries }
        return store.entries.filter { entry in
            let text = entry.title + " " + entry.body
            return words.allSatisfy { text.localizedStandardContains($0) }
        }
    }

    func load() async {
        await store.load()
        if quote == nil {
            quote = try? await fetchAyah.execute(surahNumber: 68, ayahNumber: 1)
        }
    }

    /// A blank entry for today; it's only saved once something is written in it
    func newEntry() -> JournalEntry {
        JournalEntry()
    }

    func delete(_ entry: JournalEntry) {
        store.delete(id: entry.id)
    }
}
