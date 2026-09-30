import Foundation

struct Ayah: Equatable, Identifiable, Sendable {
    let id: Int
    let number: Int
    let numberInSurah: Int
    let surahNumber: Int
    let arabicText: String
    let transliteration: String?
    let translation: String?
    let juz: Int
    let page: Int
    let hizbQuarter: Int
}
