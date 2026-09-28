import Foundation

/// The journal's entries, shared by the list and the entry screen so an edit shows in the list straight away.
/// Changes are written to the database in the background, one after another so they land in order.
@Observable
@MainActor
final class JournalStore {
    /// Newest first
    private(set) var entries: [JournalEntry] = []
    private(set) var hasLoaded = false

    private let getEntries: GetJournalEntriesUseCase
    private let saveEntry: SaveJournalEntryUseCase
    private let deleteEntry: DeleteJournalEntryUseCase
    private var lastWrite: Task<Void, Never>?

    init(getEntries: GetJournalEntriesUseCase, saveEntry: SaveJournalEntryUseCase, deleteEntry: DeleteJournalEntryUseCase) {
        self.getEntries = getEntries
        self.saveEntry = saveEntry
        self.deleteEntry = deleteEntry
    }

    func load() async {
        guard !hasLoaded else { return }
        let stored = (try? await getEntries.execute()) ?? []
        // Keeps anything written while the load was running
        let unsaved = entries.filter { entry in !stored.contains { $0.id == entry.id } }
        entries = (stored + unsaved).sorted { $0.createdAt > $1.createdAt }
        hasLoaded = true
    }

    /// Adds or updates the entry; an empty one is deleted instead, so untouched or cleared entries never linger
    func save(_ entry: JournalEntry) {
        guard !entry.isEmpty else {
            delete(id: entry.id)
            return
        }
        if let index = entries.firstIndex(where: { $0.id == entry.id }) {
            guard entries[index] != entry else { return }
            entries[index] = entry
        } else {
            entries.append(entry)
            entries.sort { $0.createdAt > $1.createdAt }
        }
        let saveEntry = saveEntry
        write { try await saveEntry.execute(entry) }
    }

    func delete(id: UUID) {
        guard let index = entries.firstIndex(where: { $0.id == id }) else { return }
        entries.remove(at: index)
        let deleteEntry = deleteEntry
        write { try await deleteEntry.execute(id: id) }
    }

    private func write(_ operation: @escaping @Sendable () async throws -> Void) {
        let previous = lastWrite
        lastWrite = Task {
            await previous?.value
            try? await operation()
        }
    }
}
