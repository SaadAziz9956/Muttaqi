import Foundation

struct ReadingProgressData: Sendable, Equatable {
    let surahNumber: Int
    let lastAyahNumber: Int
    let completedAyahs: Int
    let totalAyahs: Int
    let lastReadAt: Date

    var progressPercentage: Double {
        guard totalAyahs > 0 else { return 0 }
        return Double(completedAyahs) / Double(totalAyahs) * 100
    }
}
