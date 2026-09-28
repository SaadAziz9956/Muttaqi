import Foundation

protocol ReadingProgressRepositoryProtocol: Sendable {
    func getProgress(surahNumber: Int) async throws -> ReadingProgressData?
    func getLastRead() async throws -> ReadingProgressData?
    /// Saves the reading position and adds `readAyahs` to the ayahs already read in that surah
    func updateProgress(
        surahNumber: Int,
        lastAyahNumber: Int,
        readAyahs: Set<Int>,
        totalAyahs: Int
    ) async throws
    /// Distinct ayahs read across the whole Quran
    func totalAyahsRead() async throws -> Int
}
