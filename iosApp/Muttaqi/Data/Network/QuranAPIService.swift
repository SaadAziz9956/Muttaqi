import Foundation

// MARK: - Protocol
protocol QuranAPIServiceProtocol: Sendable {
    /// Fetch all 114 surah metadata
    func fetchSurahList() async throws -> [SurahDTO]
    /// Fetch entire Quran for a single edition (e.g. "quran-uthmani")
    func fetchFullQuran(edition: String) async throws -> FullQuranDTO
    /// Fetch single surah with ayahs for a given edition
    func fetchSurah(number: Int, edition: String) async throws -> SurahDetailDTO
}

// MARK: - Implementation
final class QuranAPIService: QuranAPIServiceProtocol {
    private let networkClient: NetworkClientProtocol

    init(networkClient: NetworkClientProtocol) {
        self.networkClient = networkClient
    }

    func fetchSurahList() async throws -> [SurahDTO] {
        guard let url = QuranEndpoint.surahList.url else {
            throw NetworkError.invalidURL
        }
        let response: APIResponse<[SurahDTO]> = try await networkClient.request(url: url)
        return response.data
    }

    func fetchFullQuran(edition: String) async throws -> FullQuranDTO {
        guard let url = QuranEndpoint.fullQuran(edition: edition).url else {
            throw NetworkError.invalidURL
        }
        let response: APIResponse<FullQuranDTO> = try await networkClient.request(url: url)
        return response.data
    }

    func fetchSurah(number: Int, edition: String) async throws -> SurahDetailDTO {
        guard let url = QuranEndpoint.surah(number: number, edition: edition).url else {
            throw NetworkError.invalidURL
        }
        let response: APIResponse<SurahDetailDTO> = try await networkClient.request(url: url)
        return response.data
    }
}
