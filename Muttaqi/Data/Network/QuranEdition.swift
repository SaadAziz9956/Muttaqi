import Foundation

enum QuranEdition {
    static let arabicUthmani = "quran-uthmani"

    static let englishTransliteration = "en.transliteration"

    static let englishSahih = "en.sahih"
    static let urduJalandhry = "ur.jalandhry"
    static let hindiHindi = "hi.hindi"

    static let englishTafsirIbnKathir = "en.ibn-kathir"

    static func translationEdition(for language: String) -> String {
        switch language {
        case "en": englishSahih
        case "ur": urduJalandhry
        case "hi": hindiHindi
        default: englishSahih
        }
    }

    static func tafsirEdition(for language: String) -> String {
        switch language {
        case "en": englishTafsirIbnKathir
        default: englishTafsirIbnKathir
        }
    }
}
