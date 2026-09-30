import Foundation

/// A hadith or verse shown on a page, e.g. under its title, in the reader's translation language. Each text is a
/// published translation, word for word; a language without one falls back to English.
struct PublishedQuote {
    let translations: [String: String]
    let source: String

    func text(language: String) -> String {
        translations[language] ?? translations["en"] ?? ""
    }
}

extension PublishedQuote {
    /// Sahih al-Bukhari 5027, under the Quran tab's title
    static let learnAndTeachQuran = PublishedQuote(
        translations: [
            // Muhsin Khan's translation (sunnah.com)
            "en": "The best among you [Muslims] are those who learn the Quran and teach it.",
            // HadeethEnc.com #5913
            "ur": "تم میں سب سے بہتر شخص وہ ہے جو قرآن سیکھے اور اسے سکھائے",
        ],
        source: "Sahih Bukhari (5027)"
    )

    /// Sahih al-Bukhari 7392, at the foot of the 99 Names page
    static let ninetyNineNames = PublishedQuote(
        // HadeethEnc.com #64673 (English v1.25.0, Urdu v1.36.0)
        translations: [
            "en": "Verily, Allah has ninety-nine names, one-hundred minus one. Whoever memorizes them all will enter Paradise.",
            "ur": "اللہ کے ننانوے یعنی ایک کم ایک سو نام ہیں، جو ان کی حفاظت کرے گا، وہ جنت میں داخل ہوگا",
        ],
        source: "Sahih al-Bukhari 7392"
    )

    /// Sahih al-Bukhari 6407, under the Dikr page's title
    static let rememberingAllah = PublishedQuote(
        // HadeethEnc.com #4177
        translations: [
            "en": "The example of the one who remembers his Lord and the one who does not remember His Lord is like the example of the living and the dead person.",
            "ur": "اس شخص کی مثال جو اپنے رب کو یاد کرتا ہے اور جو اسے یاد نہیں کرتا، زندہ اور مردہ کی سی ہے",
        ],
        source: "Sahih al-Bukhari 6407"
    )
}

extension PublishedQuote {
    /// Quran 16:125, its first sentence, at the foot of the Share page
    static let inviteWithWisdom = PublishedQuote(
        translations: [
            // Saheeh International
            "en": "Invite to the way of your Lord with wisdom and good instruction, and argue with them in a way that is best.",
            // Fateh Muhammad Jalandhry
            "ur": "(اے پیغمبر) لوگوں کو دانش اور نیک نصیحت سے اپنے پروردگار کے رستے کی طرف بلاؤ۔ اور بہت ہی اچھے طریق سے ان سے مناظرہ کرو۔",
        ],
        source: "Quran (16:125)"
    )
}
