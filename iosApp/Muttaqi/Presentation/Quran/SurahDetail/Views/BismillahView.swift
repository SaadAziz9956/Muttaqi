import SwiftUI

struct BismillahView: View {
    let text: String
    let translation: String

    var body: some View {
        VStack(spacing: 0) {
            Text(text.kfgqpcEncoded)
                .font(.arabic(18))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 44)

            Text(translation)
                .font(translationFont)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                // Urdu and Hindi fit on one line; the longer English default wraps into two balanced lines
                .lineLimit(isEnglish ? nil : 1)
                .minimumScaleFactor(isEnglish ? 1 : 0.8)
                .padding(.top, 18)
                .padding(.horizontal, isEnglish ? 60 : 16)
                .padding(.bottom, 20)
        }
    }

    // Detected from the script, not the selected language: only Al-Fatiha's translation is localised, other surahs show the English default
    private var isUrdu: Bool {
        translation.unicodeScalars.contains { (0x0600...0x06FF).contains($0.value) }
    }

    private var isHindi: Bool {
        translation.unicodeScalars.contains { (0x0900...0x097F).contains($0.value) }
    }

    private var isEnglish: Bool { !isUrdu && !isHindi }

    private var translationFont: Font {
        if isUrdu { return .urduNastaliq(14) }
        if isHindi { return .hindiDevanagari(13) }
        return .labelSmall
    }
}
