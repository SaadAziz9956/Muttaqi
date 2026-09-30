import Foundation
import SwiftData

// No longer read: the Quran text now lives in the shared Quran database. Kept in the SwiftData schema so the store,
// which also holds the journal, opens without a migration that could drop rows
@Model
final class TafsirEntity {
    var surahNumber: Int
    var ayahNumber: Int
    var language: String
    var tafsirSource: String
    var text: String

    init(
        surahNumber: Int,
        ayahNumber: Int,
        language: String,
        tafsirSource: String,
        text: String
    ) {
        self.surahNumber = surahNumber
        self.ayahNumber = ayahNumber
        self.language = language
        self.tafsirSource = tafsirSource
        self.text = text
    }
}
