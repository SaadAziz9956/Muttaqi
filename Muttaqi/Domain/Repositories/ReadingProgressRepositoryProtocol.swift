import Foundation

protocol ReadingProgressRepositoryProtocol: Sendable {
    func getProgress(surahNumber: Int) async throws -> ReadingProgressData?
    func getLastRead() async throws -> ReadingProgressData?
    func updateProgress(
        surahNumber: Int,
        lastAyahNumber: Int,
        completedAyahs: Int,
        totalAyahs: Int
    ) async throws
}
