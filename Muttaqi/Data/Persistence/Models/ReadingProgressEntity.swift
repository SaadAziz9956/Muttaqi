import Foundation
import SwiftData

@Model
final class ReadingProgressEntity {
    @Attribute(.unique) var surahNumber: Int
    var lastAyahNumber: Int
    var completedAyahs: Int
    var totalAyahs: Int
    var lastReadAt: Date

    init(
        surahNumber: Int,
        lastAyahNumber: Int,
        completedAyahs: Int,
        totalAyahs: Int,
        lastReadAt: Date = .now
    ) {
        self.surahNumber = surahNumber
        self.lastAyahNumber = lastAyahNumber
        self.completedAyahs = completedAyahs
        self.totalAyahs = totalAyahs
        self.lastReadAt = lastReadAt
    }

    var progressPercentage: Double {
        guard totalAyahs > 0 else { return 0 }
        return Double(completedAyahs) / Double(totalAyahs) * 100
    }
}
