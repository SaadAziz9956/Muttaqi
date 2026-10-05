import Shared
import SwiftUI

struct NameCard: View {
    let name: AllahName
    var minHeight: CGFloat = 300
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let meaningStyle = TranslationStyle(for: name.meaning, size: 14)

        VStack(spacing: 0) {
            Text(AttributedString.arabic(name.arabic, size: 40))
                .foregroundStyle(.textPrimary)

            Text(name.transliteration)
                .font(.custom("ReemKufi-Medium", size: 18, relativeTo: .headline))
                .foregroundStyle(.appPrimary)
                .padding(.top, 12)

            Text(name.meaning)
                .font(meaningStyle.font)
                .foregroundStyle(.textPrimary)
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
                .font(.custom("ReemKufi-Medium", size: 14, relativeTo: .subheadline))
                .foregroundStyle(.appPrimary)
                .frame(width: 36, height: 36)
                .background(Color.softSurface, in: .circle)
                .overlay { Circle().strokeBorder(Color.softRim, lineWidth: 1.5) }
                .padding(.leading, 16)
                .padding(.top, 16)
        }
        .softCard(cornerRadius: 30, rim: 3, artwork: .dawn)
        .accessibilityElement(children: .combine)
    }

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
