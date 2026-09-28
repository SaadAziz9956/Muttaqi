import Foundation

/// One dua or dhikr, with where it comes from
struct DuaEntry: Hashable, Identifiable, Sendable {
    let id: String
    let arabic: String
    let transliteration: String
    let translation: String
    /// How many times to say it; 1 when the source gives no count
    let repeatCount: Int
    /// Short English attribution, e.g. "Bukhari · Muslim"
    let source: String
    /// Full reference as the book gives it, e.g. «البخاري مع الفتح 11/113 ومسلم 4/2083»
    let reference: String
}

/// A chapter of related duas, e.g. "What to say before sleeping"
struct DuaChapter: Hashable, Identifiable, Sendable {
    let id: String
    let title: String
    let titleArabic: String?
    let entries: [DuaEntry]
}

/// A group of chapters shown as one tile on the Dua tab, e.g. "Sleep & Waking"
struct DuaCategory: Hashable, Identifiable, Sendable {
    let id: String
    let title: String
    let chapters: [DuaChapter]

    var entryCount: Int { chapters.reduce(0) { $0 + $1.entries.count } }
}
