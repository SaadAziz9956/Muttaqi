import Foundation

protocol JournalRepositoryProtocol: Sendable {
    /// Every entry, newest first
    func entries() async throws -> [JournalEntry]
    /// Inserts the entry, or updates it if one with the same id exists
    func save(_ entry: JournalEntry) async throws
    func delete(id: UUID) async throws
}
