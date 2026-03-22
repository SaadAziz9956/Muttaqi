import Foundation

protocol QuranRepositoryProtocol: Sendable {
    func getSurahs() async throws -> [Surah]
    func getSurah(number: Int) async throws -> Surah?
    func getAyahs(surahNumber: Int, language: String) async throws -> [Ayah]
    func isDataAvailable() async -> Bool
}
