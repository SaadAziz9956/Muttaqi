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
