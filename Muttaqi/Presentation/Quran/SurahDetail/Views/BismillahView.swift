import SwiftUI

struct BismillahView: View {
    let text: String
    let translation: String

    var body: some View {
        VStack(spacing: 0) {
            Text(text)
                .font(.arabic(18))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 35)

            Text(translation)
                .font(translationFont)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 18)
                .padding(.horizontal, 60)
                .padding(.bottom, 40)
        }
    }

    // Picked by script, not the selected language: only Al-Fatiha's translation is localised, other surahs show the English default
    private var translationFont: Font {
        if translation.unicodeScalars.contains(where: { (0x0600...0x06FF).contains($0.value) }) {
            return .urduNastaliq(14)
        }
        if translation.unicodeScalars.contains(where: { (0x0900...0x097F).contains($0.value) }) {
            return .hindiDevanagari(13)
        }
        return .labelSmall
    }
}
