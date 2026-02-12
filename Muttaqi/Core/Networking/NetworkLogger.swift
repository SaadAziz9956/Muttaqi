import Foundation
import os

/// Logs all HTTP requests and responses to Xcode console.
/// Uses OSLog so logs show in Console.app too.
final class NetworkLogger: Sendable {
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "Muttaqi", category: "Network")

    func logRequest(_ request: URLRequest) {
        let method = request.httpMethod ?? "GET"
        let url = request.url?.absoluteString ?? "nil"
        logger.info("➡️ \(method) \(url)")

        if let headers = request.allHTTPHeaderFields, !headers.isEmpty {
            logger.debug("  Headers: \(headers.description)")
        }
        if let body = request.httpBody, let bodyString = String(data: body, encoding: .utf8) {
            logger.debug("  Body: \(bodyString)")
        }
    }

    func logResponse(_ response: URLResponse?, data: Data?, error: Error?, duration: TimeInterval) {
        let url = response?.url?.absoluteString ?? "nil"
        let ms = String(format: "%.0fms", duration * 1000)

        if let error {
            logger.error("❌ \(url) [\(ms)] Error: \(error.localizedDescription)")
            return
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            logger.warning("⚠️ \(url) [\(ms)] Non-HTTP response")
            return
        }

        let statusCode = httpResponse.statusCode
        let dataSize = data.map { ByteCountFormatter.string(fromByteCount: Int64($0.count), countStyle: .memory) } ?? "0 bytes"
        let icon = (200...299).contains(statusCode) ? "✅" : "⚠️"

        logger.info("\(icon) \(statusCode) \(url) [\(ms)] \(dataSize)")

        // Log first 500 chars of response body for debugging
        if let data, let bodyPreview = String(data: data.prefix(500), encoding: .utf8) {
            logger.debug("  Response: \(bodyPreview)")
        }
    }
}
