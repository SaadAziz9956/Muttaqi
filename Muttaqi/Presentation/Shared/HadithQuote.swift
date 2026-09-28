import Foundation

/// A hadith shown under a page's title, in the reader's translation language. Each text is a published translation,
/// word for word; a language without one falls back to English.
struct HadithQuote {
    let translations: [String: String]
    let source: String

    func text(language: String) -> String {
        translations[language] ?? translations["en"] ?? ""
    }
}

extension HadithQuote {
    /// Sahih al-Bukhari 5027, under the Quran tab's title
    static let learnAndTeachQuran = HadithQuote(
        translations: [
            // Muhsin Khan's translation (sunnah.com)
            "en": "The best among you [Muslims] are those who learn the Quran and teach it.",
            // HadeethEnc.com #5913
            "ur": "تم میں سب سے بہتر شخص وہ ہے جو قرآن سیکھے اور اسے سکھائے",
        ],
        source: "Sahih Bukhari (5027)"
    )

    /// Sahih al-Bukhari 7392, at the foot of the 99 Names page
    static let ninetyNineNames = HadithQuote(
        // HadeethEnc.com #64673 (English v1.25.0, Urdu v1.36.0)
        translations: [
            "en": "Verily, Allah has ninety-nine names, one-hundred minus one. Whoever memorizes them all will enter Paradise.",
            "ur": "اللہ کے ننانوے یعنی ایک کم ایک سو نام ہیں، جو ان کی حفاظت کرے گا، وہ جنت میں داخل ہوگا",
        ],
        source: "Sahih al-Bukhari 7392"
    )

    /// Sahih al-Bukhari 6407, under the Dikr page's title
    static let rememberingAllah = HadithQuote(
        // HadeethEnc.com #4177
        translations: [
            "en": "The example of the one who remembers his Lord and the one who does not remember His Lord is like the example of the living and the dead person.",
            "ur": "اس شخص کی مثال جو اپنے رب کو یاد کرتا ہے اور جو اسے یاد نہیں کرتا، زندہ اور مردہ کی سی ہے",
        ],
        source: "Sahih al-Bukhari 6407"
    )
}
