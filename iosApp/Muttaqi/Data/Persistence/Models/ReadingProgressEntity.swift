import Foundation
import SwiftData

// Read once at launch by StoredReadingProgressImport, which hands it to the shared Quran database; kept in the schema
// so the store, which also holds the journal, opens without a migration
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
