import Foundation

protocol TafsirRepositoryProtocol: Sendable {
    func getTafsir(surahNumber: Int, languageCode: String) async throws -> [TafsirAyah]
}
