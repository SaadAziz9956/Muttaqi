import Foundation
import SwiftData

// No longer read: the Quran text now lives in the shared Quran database. Kept in the SwiftData schema so the store,
// which also holds the journal, opens without a migration that could drop rows
@Model
final class AyahTranslationEntity {
    var ayahNumber: Int
    var language: String
    var editionIdentifier: String
    var text: String

    var ayah: AyahEntity?

    init(
        ayahNumber: Int,
        language: String,
        editionIdentifier: String,
        text: String
    ) {
        self.ayahNumber = ayahNumber
        self.language = language
        self.editionIdentifier = editionIdentifier
        self.text = text
    }
}
