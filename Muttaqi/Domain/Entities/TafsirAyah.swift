import Foundation

struct TafsirAyah: Equatable, Identifiable, Sendable {
    let id: Int
    let ayahNumber: Int
    /// Commentary often covers a group of ayahs; this is the last one in the group (equal to `ayahNumber` for a single ayah)
    let lastAyahNumber: Int
    let verseKey: String
    let text: String
}
