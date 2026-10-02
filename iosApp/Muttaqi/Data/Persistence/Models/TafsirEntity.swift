import Foundation
import SwiftData

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
