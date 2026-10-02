import Foundation
import SwiftData

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
