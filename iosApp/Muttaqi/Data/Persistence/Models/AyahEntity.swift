import Foundation
import SwiftData

@Model
final class AyahEntity {
    @Attribute(.unique) var number: Int
    var numberInSurah: Int
    var surahNumber: Int
    var arabicText: String
    var transliteration: String?
    var juz: Int
    var page: Int
    var hizbQuarter: Int

    var surah: SurahEntity?

    @Relationship(deleteRule: .cascade, inverse: \AyahTranslationEntity.ayah)
    var translations: [AyahTranslationEntity]

    init(
        number: Int,
        numberInSurah: Int,
        surahNumber: Int,
        arabicText: String,
        transliteration: String?,
        juz: Int,
        page: Int,
        hizbQuarter: Int
    ) {
        self.number = number
        self.numberInSurah = numberInSurah
        self.surahNumber = surahNumber
        self.arabicText = arabicText
        self.transliteration = transliteration
        self.juz = juz
        self.page = page
        self.hizbQuarter = hizbQuarter
        self.translations = []
    }
}
