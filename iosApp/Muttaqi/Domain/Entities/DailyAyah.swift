import Foundation

/// An ayah together with its surah, so it can be shown with its reference and opened in the reader
struct DailyAyah: Equatable, Sendable {
    let surah: Surah
    let ayah: Ayah

    var reference: String { "\(surah.number):\(ayah.numberInSurah)" }
}
