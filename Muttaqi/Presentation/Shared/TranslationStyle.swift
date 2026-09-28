import SwiftUI

extension String {
    /// Starts a translation cut from mid-sentence, e.g. "glory be to Allah", with a capital; the words stay as published
    var sentenceCased: String {
        prefix(1).uppercased() + dropFirst()
    }

    /// In quotation marks that face the right way for its script: Urdu opens with ” and closes with “, as its
    /// publishers print it
    var quoted: String {
        TranslationStyle.isArabicScript(self) ? "\u{201D}\(self)\u{201C}" : "\u{201C}\(self)\u{201D}"
    }
}

/// Font and direction for translation text, chosen from the text's own script, so an English fallback is never
/// set in a Nastaliq or Devanagari font
struct TranslationStyle {
    let font: Font
    let isRightToLeft: Bool

    init(for text: String, size: CGFloat) {
        if Self.isArabicScript(text) {
            font = .urduNastaliq(size + 1)
            isRightToLeft = true
        } else if text.unicodeScalars.contains(where: { (0x0900...0x097F).contains($0.value) }) {
            font = .hindiDevanagari(size)
            isRightToLeft = false
        } else {
            font = .custom("ReemKufi-Regular", size: size)
            isRightToLeft = false
        }
    }

    /// Urdu and other text in the Arabic script, which runs right to left
    static func isArabicScript(_ text: String) -> Bool {
        text.unicodeScalars.contains { (0x0600...0x06FF).contains($0.value) }
    }
}
