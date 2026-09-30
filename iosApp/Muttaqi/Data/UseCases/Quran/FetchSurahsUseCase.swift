import Foundation

struct FetchSurahsUseCase: Sendable {
    private let repository: QuranRepositoryProtocol

    init(repository: QuranRepositoryProtocol) {
        self.repository = repository
    }

    func execute() async throws -> [Surah] {
        try await repository.getSurahs()
    }
}
