import Foundation
import SwiftData

@Model
final class ReadingProgressEntity {
    @Attribute(.unique) var surahNumber: Int
    var lastAyahNumber: Int
    /// How many distinct ayahs of this surah have been read (the count of `readAyahs`)
    var completedAyahs: Int
    var totalAyahs: Int
    var lastReadAt: Date
    /// Every ayah of this surah that has been on screen while reading, by number within the surah.
    /// Defaulted so stores from before it existed migrate without losing their rows.
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
