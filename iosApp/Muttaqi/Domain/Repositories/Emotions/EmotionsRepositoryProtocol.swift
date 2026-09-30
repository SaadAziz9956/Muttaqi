import Foundation

protocol EmotionsRepositoryProtocol: Sendable {
    /// Every emotion in order, with translations in `language` (English where there's no published one in it)
    func emotions(language: String) throws -> [Emotion]
    func header(language: String) throws -> PageQuote
}
