import Foundation

enum NetworkError: LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int)
    case decodingError(Error)
    case noData
    case unknown(Error)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            "Invalid URL"
        case .invalidResponse:
            "Invalid server response"
        case .httpError(let statusCode):
            "Server error: \(statusCode)"
        case .decodingError(let error):
            "Failed to decode: \(error.localizedDescription)"
        case .noData:
            "No data received"
        case .unknown(let error):
            error.localizedDescription
        }
    }
}
