import Foundation
import SwiftData

@ModelActor
actor JournalRepository: JournalRepositoryProtocol {

    func entries() async throws -> [JournalEntry] {
        let descriptor = FetchDescriptor<JournalEntryEntity>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        return try modelContext.fetch(descriptor).map { $0.toEntry() }
    }

    func save(_ entry: JournalEntry) async throws {
        let id = entry.id
        let descriptor = FetchDescriptor<JournalEntryEntity>(
            predicate: #Predicate { $0.id == id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.title = entry.title
            existing.body = entry.body
            existing.updatedAt = entry.updatedAt
        } else {
            modelContext.insert(JournalEntryEntity(
                id: entry.id,
                title: entry.title,
                body: entry.body,
                createdAt: entry.createdAt,
                updatedAt: entry.updatedAt
            ))
        }
        try modelContext.save()
    }

    func delete(id: UUID) async throws {
        try modelContext.delete(model: JournalEntryEntity.self, where: #Predicate { $0.id == id })
        try modelContext.save()
    }
}

private extension JournalEntryEntity {
    func toEntry() -> JournalEntry {
        JournalEntry(id: id, title: title, body: body, createdAt: createdAt, updatedAt: updatedAt)
    }
}
