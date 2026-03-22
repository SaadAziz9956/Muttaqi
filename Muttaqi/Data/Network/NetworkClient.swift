import Foundation

protocol NetworkClientProtocol: Sendable {
    func request<T: Decodable>(url: URL) async throws -> T
}

final class NetworkClient: NetworkClientProtocol {
    private let session: URLSession
    private let decoder: JSONDecoder
    private let logger: NetworkLogger

    init(session: URLSession = .shared, logger: NetworkLogger = NetworkLogger()) {
        self.session = session
        self.decoder = JSONDecoder()
        self.logger = logger
    }

    func request<T: Decodable>(url: URL) async throws -> T {
        let request = URLRequest(url: url)
        logger.logRequest(request)

        let start = CFAbsoluteTimeGetCurrent()
        let data: Data
        let response: URLResponse

        do {
            (data, response) = try await session.data(for: request)
        } catch {
            let duration = CFAbsoluteTimeGetCurrent() - start
            logger.logResponse(nil, data: nil, error: error, duration: duration)
            throw NetworkError.unknown(error)
        }

        let duration = CFAbsoluteTimeGetCurrent() - start
        logger.logResponse(response, data: data, error: nil, duration: duration)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.httpError(statusCode: httpResponse.statusCode)
        }

        do {
            return try decoder.decode(T.self, from: data)
        } catch {
            throw NetworkError.decodingError(error)
        }
    }
}
