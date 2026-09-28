import Foundation

struct GetJournalEntriesUseCase: Sendable {
    private let repository: JournalRepositoryProtocol

    init(repository: JournalRepositoryProtocol) {
        self.repository = repository
    }

    func execute() async throws -> [JournalEntry] {
        try await repository.entries()
    }
}
