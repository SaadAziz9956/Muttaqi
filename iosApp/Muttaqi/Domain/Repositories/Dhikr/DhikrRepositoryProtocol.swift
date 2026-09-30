import Foundation

protocol DhikrRepositoryProtocol: Sendable {
    /// Every dhikr, grouped into sections in the order they're shown, with translations in `language`
    /// (English where there's no published translation in it)
    func sections(language: String) throws -> [DhikrSection]
}
