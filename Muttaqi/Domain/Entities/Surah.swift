import Foundation

struct Surah: Equatable, Identifiable, Sendable {
    let id: Int
    let number: Int
    let name: String
    let englishName: String
    let englishNameTranslation: String
    let revelationType: String
    let numberOfAyahs: Int
}
