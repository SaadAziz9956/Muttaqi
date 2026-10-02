import SwiftUI

extension String {
    var sentenceCased: String {
        prefix(1).uppercased() + dropFirst()
    }

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
        text.unicodeScalars.contains { (0x0600...0x06FF).contains($0.value) }
    }
}
