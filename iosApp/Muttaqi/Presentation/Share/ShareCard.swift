import Shared
import SwiftUI

/// The card on the Share page, and the image that's shared: the text on the brand green with the app's name behind it
struct ShareCard: View {
    let passage: Shared.SharePassage

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
        .padding(.vertical, 36)
        .frame(maxWidth: .infinity)
        .background {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 150))
                .foregroundStyle(.white.opacity(0.06))
                .fixedSize()
                .accessibilityHidden(true)
        }
        .background(.shareCard)
        .clipShape(.rect(cornerRadius: 15))
        .shadow(color: .black.opacity(0.25), radius: 5, y: 1)
    }
}

extension ShareCard {
    /// The card as an image to share, with a margin of the same green so it reads as one picture
    @MainActor
    static func image(of passage: Shared.SharePassage) -> UIImage? {
        let width: CGFloat = 390
        let renderer = ImageRenderer(
            content: ShareCard(passage: passage)
                // As tall as the text needs at this width, so no line is cut short
                .fixedSize(horizontal: false, vertical: true)
                .padding(24)
                .frame(width: width)
                .background(.shareCard)
        )
        renderer.proposedSize = ProposedViewSize(width: width, height: nil)
        renderer.scale = 3
        return renderer.uiImage
    }
}
