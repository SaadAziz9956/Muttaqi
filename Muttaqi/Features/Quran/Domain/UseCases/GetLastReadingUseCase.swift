import Foundation

struct GetLastReadingUseCase: Sendable {
    private let progressRepository: ReadingProgressRepositoryProtocol
    private let quranRepository: QuranRepositoryProtocol

    init(
        progressRepository: ReadingProgressRepositoryProtocol,
        quranRepository: QuranRepositoryProtocol
    ) {
        self.progressRepository = progressRepository
        self.quranRepository = quranRepository
    }

    func execute() async throws -> ReadingProgress? {
        guard let entity = try await progressRepository.getLastRead() else {
            return nil
        }
        guard let surah = try await quranRepository.getSurah(number: entity.surahNumber) else {
            return nil
        }
        return ReadingProgress(
            surahNumber: entity.surahNumber,
            surahName: surah.name,
            surahEnglishName: surah.englishName,
            lastAyahNumber: entity.lastAyahNumber,
            completedAyahs: entity.completedAyahs,
            totalAyahs: entity.totalAyahs,
            lastReadAt: entity.lastReadAt
        )
    }
}
