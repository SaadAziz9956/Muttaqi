import Foundation

protocol NamesRepositoryProtocol: Sendable {
    /// The 99 names in order, with meanings in `language` (English where there's no published translation in it)
    func names(language: String) throws -> [AllahName]
}
