import Foundation

struct ReadingProgress: Equatable, Sendable {
    /// Where the reader left off
    let surahNumber: Int
    let surahName: String
    let surahEnglishName: String
    let lastAyahNumber: Int
    let lastReadAt: Date
    /// Distinct ayahs read across the whole Quran, and how many ayahs the Quran has
    let quranAyahsRead: Int
    let quranTotalAyahs: Int

    /// Share of the whole Quran read so far, from 0 to 1
    var quranCompletion: Double {
        guard quranTotalAyahs > 0 else { return 0 }
        return min(Double(quranAyahsRead) / Double(quranTotalAyahs), 1)
    }

    var quranAyahsLeft: Int { max(quranTotalAyahs - quranAyahsRead, 0) }
}
