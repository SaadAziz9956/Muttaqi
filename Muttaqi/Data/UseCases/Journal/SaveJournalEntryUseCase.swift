import Foundation

struct SaveJournalEntryUseCase: Sendable {
    private let repository: JournalRepositoryProtocol

    init(repository: JournalRepositoryProtocol) {
        self.repository = repository
    }

    func execute(_ entry: JournalEntry) async throws {
        try await repository.save(entry)
    }
}
