import Foundation

struct ReadingProgress: Equatable, Sendable {
    let surahNumber: Int
    let surahName: String
    let surahEnglishName: String
    let lastAyahNumber: Int
    let completedAyahs: Int
    let totalAyahs: Int
    let lastReadAt: Date

    var progressPercentage: Double {
        guard totalAyahs > 0 else { return 0 }
        return Double(completedAyahs) / Double(totalAyahs) * 100
    }
}
