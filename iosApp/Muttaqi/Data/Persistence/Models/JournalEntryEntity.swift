import Foundation
import SwiftData

/// A journal entry as the app kept it before the journal moved to the shared database. Kept in the schema only so
/// `SwiftDataJournalImport` can read these once; nothing writes them any more
@Model
final class JournalEntryEntity {
    @Attribute(.unique) var id: UUID
    var title: String
    var body: String
    var createdAt: Date
    var updatedAt: Date

    init(id: UUID, title: String, body: String, createdAt: Date, updatedAt: Date) {
        self.id = id
        self.title = title
        self.body = body
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
