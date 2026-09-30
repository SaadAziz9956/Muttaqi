import Foundation

/// One of the Beautiful Names of Allah (al-Asma' al-Husna)
struct AllahName: Identifiable, Hashable, Sendable {
    let number: Int
    let arabic: String
    let transliteration: String
    /// Short meaning, in a published translation
    let meaning: String

    var id: Int { number }
}
