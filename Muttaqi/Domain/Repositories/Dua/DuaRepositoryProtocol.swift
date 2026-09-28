import Foundation

protocol DuaRepositoryProtocol: Sendable {
    /// The Quranic duas (Rabbana and Rabbi) used for the Dua of the Day
    func duas(language: String) throws -> [Dua]
    /// Every dua category: the Quranic duas followed by the categories of Hisn al-Muslim
    func categories(language: String) throws -> [DuaCategory]
}
