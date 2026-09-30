import Foundation

protocol ExploreRepositoryProtocol: Sendable {
    /// Every group and its topics in order, with translations in `language` (English where there's no published one)
    func groups(language: String) throws -> [ExploreGroup]
    func header(language: String) throws -> PageQuote
}
