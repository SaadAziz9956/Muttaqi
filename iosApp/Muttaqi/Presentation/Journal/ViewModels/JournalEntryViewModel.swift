import Foundation

/// Edits one entry. Saves on its own a moment after typing stops, like Notes, so there's no Save button.
@Observable
@MainActor
final class JournalEntryViewModel {
    var title: String {
        didSet { if title != oldValue { edited() } }
    }
    var body: String {
        didSet { if body != oldValue { edited() } }
    }
    let createdAt: Date
    /// Opened blank, e.g. from the new-entry button, so the title gets the keyboard straight away
    let startedEmpty: Bool

    private let id: UUID
    private var updatedAt: Date
    private let store: JournalStore
    private var saveTask: Task<Void, Never>?
    private var isDeleted = false

    init(entry: JournalEntry, store: JournalStore) {
        self.id = entry.id
        self.title = entry.title
        self.body = entry.body
        self.createdAt = entry.createdAt
        self.updatedAt = entry.updatedAt
        self.startedEmpty = entry.isEmpty
        self.store = store
    }

    var isEmpty: Bool { currentEntry.isEmpty }

    /// Writes any pending change now, e.g. when the reader leaves the screen or the app
    func saveNow() {
        saveTask?.cancel()
        guard !isDeleted else { return }
        store.save(currentEntry)
    }

    func delete() {
        saveTask?.cancel()
        isDeleted = true
        store.delete(id: id)
    }

    private var currentEntry: JournalEntry {
        JournalEntry(id: id, title: title, body: body, createdAt: createdAt, updatedAt: updatedAt)
    }

    private func edited() {
        updatedAt = .now
        saveTask?.cancel()
        saveTask = Task { [weak self] in
            try? await Task.sleep(for: .milliseconds(500))
            guard !Task.isCancelled else { return }
            self?.saveNow()
        }
    }
}
