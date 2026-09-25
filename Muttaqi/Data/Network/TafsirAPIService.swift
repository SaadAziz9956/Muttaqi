import Foundation

protocol TafsirAPIServiceProtocol: Sendable {
    func fetchTafsir(tafsirId: Int, surahNumber: Int) async throws -> TafsirResponse
}

final class TafsirAPIService: TafsirAPIServiceProtocol {
    private let networkClient: NetworkClientProtocol

    init(networkClient: NetworkClientProtocol) {
        self.networkClient = networkClient
    }

    func fetchTafsir(tafsirId: Int, surahNumber: Int) async throws -> TafsirResponse {
        guard let url = TafsirEndpoint.tafsirByChapter(tafsirId: tafsirId, chapterNumber: surahNumber).url else {
            throw NetworkError.invalidURL
        }
        return try await networkClient.request(url: url)
    }
}
