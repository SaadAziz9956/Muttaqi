import Foundation

struct UpdateReadingProgressUseCase: Sendable {
    private let repository: ReadingProgressRepositoryProtocol

    init(repository: ReadingProgressRepositoryProtocol) {
        self.repository = repository
    }

    func execute(
        surahNumber: Int,
        lastAyahNumber: Int,
        readAyahs: Set<Int>,
        totalAyahs: Int
    ) async throws {
        try await repository.updateProgress(
            surahNumber: surahNumber,
            lastAyahNumber: lastAyahNumber,
            readAyahs: readAyahs,
            totalAyahs: totalAyahs
        )
    }
}
