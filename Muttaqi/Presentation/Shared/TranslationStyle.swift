import SwiftUI

/// Font and direction for translation text, chosen from the text's own script, so an English fallback is never
/// set in a Nastaliq or Devanagari font
struct TranslationStyle {
    let font: Font
    let isRightToLeft: Bool

    init(for text: String, size: CGFloat) {
        if text.unicodeScalars.contains(where: { (0x0600...0x06FF).contains($0.value) }) {
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
}
