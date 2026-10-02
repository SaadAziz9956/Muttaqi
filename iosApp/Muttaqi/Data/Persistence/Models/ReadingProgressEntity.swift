import Foundation
import SwiftData

@Model
final class ReadingProgressEntity {
    @Attribute(.unique) var surahNumber: Int
    var lastAyahNumber: Int
    var completedAyahs: Int
    var totalAyahs: Int
    var lastReadAt: Date
    var readAyahs: [Int] = []

    init(
        surahNumber: Int,
        lastAyahNumber: Int,
        readAyahs: [Int],
        totalAyahs: Int,
        lastReadAt: Date = .now
    ) {
        self.surahNumber = surahNumber
        self.lastAyahNumber = lastAyahNumber
        self.readAyahs = readAyahs
        self.completedAyahs = readAyahs.count
        self.totalAyahs = totalAyahs
        self.lastReadAt = lastReadAt
    }
}
