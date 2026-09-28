import Foundation

struct ReadingProgress: Equatable, Sendable {
    /// Where the reader left off
    let surahNumber: Int
    let surahName: String
    let surahEnglishName: String
    let lastAyahNumber: Int
    let lastReadAt: Date
}
