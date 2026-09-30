import Foundation

/// A Quranic supplication: the dua portion of an ayah, in the app's own Quran text and translations
struct Dua: Equatable, Identifiable, Sendable {
    let surahNumber: Int
    let ayahNumber: Int
    let arabic: String
    let transliteration: String
    let translation: String

    var id: String { "\(surahNumber):\(ayahNumber)" }
}
