import Foundation

/// A feeling, with Quran verses, authentic hadith and duas that speak to it, in the reader's language
struct Emotion: Identifiable, Hashable, Sendable {
    let id: String
    let title: String
    let verses: [EmotionVerse]
    let hadith: [EmotionHadith]
    /// Duas from Hisn al-Muslim
    let duas: [DuaEntry]
}

struct EmotionVerse: Hashable, Sendable {
    /// e.g. "3:134"
    let reference: String
    let arabic: String
    /// Published translation, word for word
    let translation: String
    let credit: String
}

struct EmotionHadith: Hashable, Sendable {
    /// Published translation, in full as the publisher asks
    let translation: String
    /// Who narrated it in the collections, e.g. "Agreed upon"
    let attribution: String
    let grade: String
    let credit: String
}

/// The verse under the Emotions page's title
struct EmotionsHeader: Hashable, Sendable {
    let reference: String
    let translation: String
}
