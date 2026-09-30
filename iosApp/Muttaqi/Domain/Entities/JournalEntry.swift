import Foundation

/// One entry in the gratitude journal
struct JournalEntry: Identifiable, Hashable, Sendable {
    let id: UUID
    var title: String
    var body: String
    /// The day the entry was written, shown in the list and on the entry
    let createdAt: Date
    var updatedAt: Date

    init(id: UUID = UUID(), title: String = "", body: String = "", createdAt: Date = .now, updatedAt: Date = .now) {
        self.id = id
        self.title = title
        self.body = body
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    /// Nothing worth keeping, so an untouched or cleared entry is never saved
    var isEmpty: Bool {
        title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && body.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    /// The line shown in the list: the title, or the body's first line when there's no title
    var preview: String {
        let title = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard title.isEmpty else { return title }
        return body.split(whereSeparator: \.isNewline)
            .lazy
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .first { !$0.isEmpty } ?? ""
    }
}
