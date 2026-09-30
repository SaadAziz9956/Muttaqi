import Foundation

/// A phrase of remembrance, how many times the hadith says to repeat it, and why
struct Dhikr: Identifiable, Hashable, Sendable {
    /// One phrase of a set said in parts, e.g. SubhanAllah 33 times before moving on to Alhamdulillah
    struct Step: Hashable, Sendable {
        let arabic: String
        let transliteration: String
        /// Published translation; nil when there's none
        let translation: String?
        let count: Int
    }

    let id: String
    /// Names a set of phrases, e.g. "After every prayer · 33, 33 and 34"; nil for a single phrase
    let title: String?
    let arabic: String
    let transliteration: String
    /// Published translation of the words; nil when there's none, e.g. where a translator left them in Arabic
    let translation: String?
    /// Empty unless the dhikr is a set said in parts
    let steps: [Step]
    /// Times to say a single phrase; nil when the hadith gives no number
    let count: Int?
    /// The hadith giving its virtue, in a published translation; nil when there's none, e.g. for some verses
    let hadith: String?
    let reference: String
    let grade: String
    /// Whose translations are shown, e.g. "HadeethEnc.com"
    let credit: String?

    /// Times to say it in one go, the steps' counts added up for a set; nil for open-ended remembrance
    var target: Int? {
        steps.isEmpty ? count : steps.reduce(0) { $0 + $1.count }
    }
}

struct DhikrSection: Identifiable, Hashable, Sendable {
    let id: String
    let title: String
    let subtitle: String
    let dhikr: [Dhikr]
}
