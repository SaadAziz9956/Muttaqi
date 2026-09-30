import Foundation

/// A Quran verse, or a few in a row, as shown on a topic page, in the reader's language
struct QuranPassage: Hashable, Sendable {
    /// e.g. "3:134", or "59:22-24" for verses in a row
    let reference: String
    let arabic: String
    /// Published translation, word for word
    let translation: String
    let credit: String
}

/// An authentic hadith as shown on a topic page, in the reader's language
struct HadithPassage: Hashable, Sendable {
    let arabic: String
    /// Published translation, in full as the publisher asks
    let translation: String
    /// Who narrated it in the collections, e.g. "Agreed upon"
    let attribution: String
    let grade: String
    let credit: String
}

/// The verse quoted under a page's title
struct PageQuote: Hashable, Sendable {
    let reference: String
    let translation: String
}

/// Anything shown as a page of Quran verses, hadith and duas, such as an emotion or an Explore topic
protocol PassageTopic: Identifiable where ID == String {
    var title: String { get }
    var verses: [QuranPassage] { get }
    var hadith: [HadithPassage] { get }
    /// Duas from Hisn al-Muslim
    var duas: [DuaEntry] { get }
}
