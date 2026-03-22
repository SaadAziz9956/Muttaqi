import Foundation

struct Language: Hashable, Codable, Sendable {
    let code: String
    let name: String
    
    static let english = Language(code: "en", name: "English")
    static let urdu = Language(code: "ur", name: "Urdu")
    static let hindi = Language(code: "hi", name: "Hindi")
    
    static let available: [Language] = [.english, .urdu, .hindi]
    
    static func from(code: String) -> Language {
        available.first { $0.code == code } ?? .english
    }
}
