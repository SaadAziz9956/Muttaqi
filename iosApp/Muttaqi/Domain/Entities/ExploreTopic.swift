import Foundation

/// A subject in Explore, such as Fasting or Honesty, with the Quran verses, authentic hadith and duas about it
struct ExploreTopic: PassageTopic, Hashable, Sendable {
    let id: String
    let title: String
    /// Iconsax icon in the asset catalogue, e.g. "drop-linear"
    let icon: String
    /// Other words a reader might search for, e.g. "zakah" and "sadaqah" for Charity & Zakat
    let keywords: [String]
    let verses: [QuranPassage]
    let hadith: [HadithPassage]
    let duas: [DuaEntry]
}

/// A heading on the Explore page, such as Worship, and its topics
struct ExploreGroup: Identifiable, Hashable, Sendable {
    let id: String
    let title: String
    let topics: [ExploreTopic]
}
