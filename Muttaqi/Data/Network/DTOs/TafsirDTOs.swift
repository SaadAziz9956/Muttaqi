import Foundation

struct TafsirResponse: Decodable {
    let tafsirs: [TafsirAyahDTO]
}

struct TafsirAyahDTO: Decodable {
    let id: Int
    let verseKey: String
    let text: String

    enum CodingKeys: String, CodingKey {
        case id, text
        case verseKey = "verse_key"
    }
}
