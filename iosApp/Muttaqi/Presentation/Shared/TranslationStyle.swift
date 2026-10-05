import SwiftUI

extension String {
    var quoted: String {
        TranslationStyle.isArabicScript(self) ? "\u{201D}\(self)\u{201C}" : "\u{201C}\(self)\u{201D}"
    }
}

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

    static func isArabicScript(_ text: String) -> Bool {
        guard let letter = text.unicodeScalars.first(where: { $0.properties.isAlphabetic }) else { return false }
        return [0x0600...0x06FF, 0x0750...0x077F, 0xFB50...0xFDFF, 0xFE70...0xFEFF].contains { $0.contains(letter.value) }
    }
}
