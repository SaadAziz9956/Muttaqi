import SwiftUI

/// One name: its number, Arabic, transliteration and meaning, over a large faint copy of the Arabic
struct NameCard: View {
    let name: AllahName
    /// Grows with the screen on the swiping page; a card grows further if its text needs the room
    var minHeight: CGFloat = 300
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let meaningStyle = TranslationStyle(for: name.meaning, size: 14)

        VStack(spacing: 0) {
            Text(AttributedString.arabic(name.arabic, size: 40))
                .foregroundStyle(.textPrimary)

            Text(name.transliteration)
                .font(.custom("ReemKufi-Regular", size: 18, relativeTo: .headline))
                .foregroundStyle(.textSecondary)
                .padding(.top, 12)

            Text(name.meaning.sentenceCased)
                .font(meaningStyle.font)
                .foregroundStyle(.textSecondary)
                .lineSpacing(meaningStyle.isRightToLeft ? 6 : 2)
                .padding(.top, 10)
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, 20)
        .padding(.vertical, 48)
        .frame(maxWidth: .infinity, minHeight: minHeight)
        .background { watermark }
        .overlay(alignment: .topLeading) {
            Text("\(name.number)")
                .font(.custom("ReemKufi-Regular", size: 18, relativeTo: .headline))
                .foregroundStyle(.textPrimary)
                .padding(.leading, 18)
                .padding(.top, 14)
        }
        .background(.tintedSurface, in: .rect(cornerRadius: 15))
        .shadow(color: Color.brandTeal.opacity(0.25), radius: 2, y: 1)
        .accessibilityElement(children: .combine)
    }

    // The design's faint Kufic copy of the name behind the text; fainter in dark mode, where the lighter teal on a dark
    // card stands out more
    private var watermark: some View {
        Text(name.arabic)
            .font(.custom("ReemKufi-Regular", size: 90))
            .foregroundStyle(Color.brandTeal.opacity(colorScheme == .dark ? 0.08 : 0.11))
            .lineLimit(1)
            .minimumScaleFactor(0.4)
            .padding(.horizontal, 12)
            .offset(y: 24)
            .accessibilityHidden(true)
    }
}
