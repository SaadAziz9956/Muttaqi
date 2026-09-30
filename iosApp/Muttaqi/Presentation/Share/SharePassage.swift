import Foundation
import Shared

/// A verse, hadith, dua, dhikr or Name of Allah to share as a card, in the reader's language
struct SharePassage: Hashable {
    /// Empty when there is none, e.g. a hadith shown only in translation
    let arabic: String
    var transliteration: String?
    let translation: String
    /// Where it's from, e.g. "Quran (2:255)" or "Narrated by Muslim · Authentic"
    let reference: String
}

extension SharePassage {
    /// A passage from the shared code, e.g. a dua the chapter's view model asked to share
    init(_ passage: Shared.SharePassage) {
        self.init(arabic: passage.arabic, transliteration: passage.transliteration, translation: passage.translation, reference: passage.reference)
    }

    init(verse: QuranPassage) {
        self.init(arabic: verse.arabic, translation: verse.translation, reference: "Quran (\(verse.reference))")
    }

    init(hadith: HadithPassage) {
        self.init(arabic: hadith.arabic, translation: hadith.translation, reference: "\(hadith.attribution) · \(hadith.grade)")
    }

    init(dua: DuaEntry) {
        self.init(arabic: dua.arabic, transliteration: dua.transliteration, translation: dua.translation, reference: dua.source)
    }

    init(ayah: Ayah) {
        self.init(
            // Without the end-of-ayah sign, which the card doesn't number
            arabic: ayah.arabicText.replacingOccurrences(of: "\u{06DD}", with: "").trimmingCharacters(in: .whitespaces),
            translation: ayah.translation ?? "",
            reference: "Quran (\(ayah.surahNumber):\(ayah.numberInSurah))"
        )
    }

    init(dhikr: Dhikr) {
        self.init(
            arabic: dhikr.arabic,
            transliteration: dhikr.transliteration,
            translation: dhikr.translation ?? dhikr.steps.compactMap(\.translation).joined(separator: "\n"),
            reference: dhikr.reference
        )
    }

    init(name: AllahName) {
        self.init(
            arabic: name.arabic,
            transliteration: name.transliteration,
            translation: name.meaning,
            reference: "The Names of Allah (\(name.number) of 99)"
        )
    }
}
