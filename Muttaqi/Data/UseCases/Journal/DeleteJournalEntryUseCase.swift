import Foundation

struct DeleteJournalEntryUseCase: Sendable {
    private let repository: JournalRepositoryProtocol

    init(repository: JournalRepositoryProtocol) {
        self.repository = repository
    }

    func execute(id: UUID) async throws {
        try await repository.delete(id: id)
    }
}
