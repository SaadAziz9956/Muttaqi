import SwiftUI

struct BismillahView: View {
    let text: String
    let translation: String?

    var body: some View {
        VStack(spacing: 0) {
            Text(text)
                .font(.arabic(18))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 44)
                .padding(.bottom, translation == nil ? 20 : 0)

            if let translation {
                Text(translation)
                    .font(translationFont(translation))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.top, 18)
                    .padding(.horizontal, isEnglish(translation) ? 60 : 16)
                    .padding(.bottom, 20)
            }
        }
    }

    private func isUrdu(_ translation: String) -> Bool {
        translation.unicodeScalars.contains { (0x0600...0x06FF).contains($0.value) }
    }

    private func isHindi(_ translation: String) -> Bool {
        translation.unicodeScalars.contains { (0x0900...0x097F).contains($0.value) }
    }

    private func isEnglish(_ translation: String) -> Bool { !isUrdu(translation) && !isHindi(translation) }

    private func translationFont(_ translation: String) -> Font {
        if isUrdu(translation) { return .urduNastaliq(14) }
        if isHindi(translation) { return .hindiDevanagari(13) }
        return .labelSmall
    }
}
