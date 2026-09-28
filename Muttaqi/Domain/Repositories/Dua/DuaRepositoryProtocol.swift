import Foundation

protocol DuaRepositoryProtocol: Sendable {
    func duas(language: String) throws -> [Dua]
}
