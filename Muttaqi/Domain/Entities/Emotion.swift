import Foundation

/// A feeling, with Quran verses, authentic hadith and duas that speak to it, in the reader's language
struct Emotion: PassageTopic, Hashable, Sendable {
    let id: String
    let title: String
    let verses: [QuranPassage]
    let hadith: [HadithPassage]
    let duas: [DuaEntry]
}
