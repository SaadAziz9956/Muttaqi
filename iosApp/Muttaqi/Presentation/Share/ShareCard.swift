import Shared
import SwiftUI

struct ShareCard: View {
    let passage: SharePassage

    var body: some View {
        let translationStyle = TranslationStyle(for: passage.translation, size: 14)

        VStack(spacing: 14) {
            if !passage.arabic.isEmpty {
                Text(AttributedString.arabic(passage.arabic, size: 20))
                    .lineSpacing(10)
            }

            if let transliteration = passage.transliteration, !transliteration.isEmpty {
                Text(transliteration)
                    .font(.custom("ReemKufi-Regular", size: 14))
                    .foregroundStyle(.brandTeal)
            }

            if !passage.translation.isEmpty {
                Text(passage.translation)
                    .font(translationStyle.font)
                    .lineSpacing(translationStyle.isRightToLeft ? 8 : 4)
            }

            Text(passage.reference)
                .font(TranslationStyle.isArabicScript(passage.reference)
                    ? TranslationStyle(for: passage.reference, size: 11).font
                    : .custom("ReemKufi-Regular", size: 12))
                .foregroundStyle(.brandTeal)
        }
        .foregroundStyle(.white)
        .multilineTextAlignment(.center)
        .padding(.horizontal, 24)
        .padding(.vertical, 40)
        .frame(maxWidth: .infinity)
        .background {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 150))
                .foregroundStyle(.white.opacity(0.07))
                .fixedSize()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .clipShape(.rect(cornerRadius: 28, style: .continuous))
                .accessibilityHidden(true)
        }
        .softCard(rim: 3, artwork: .forest)
    }
}

extension ShareCard {
    @MainActor
    static func image(of passage: SharePassage) -> UIImage? {
        let width: CGFloat = 390
        let renderer = ImageRenderer(
            content: ShareCard(passage: passage)
                .fixedSize(horizontal: false, vertical: true)
                .padding(24)
                .frame(width: width)
                .background { SoftBackdrop() }
                .environment(\.colorScheme, .light)
        )
        renderer.proposedSize = ProposedViewSize(width: width, height: nil)
        renderer.scale = 3
        return renderer.uiImage
    }
}
