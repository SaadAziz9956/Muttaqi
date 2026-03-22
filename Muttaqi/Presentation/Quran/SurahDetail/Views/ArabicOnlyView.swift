import SwiftUI

struct ArabicOnlyView: View {
    let ayahs: [Ayah]
    let fontSize: Int

    var body: some View {
        let arabicText = buildArabicText()

        Text(arabicText)
            .font(.arabic(CGFloat(fontSize)))
            .foregroundStyle(.textPrimary)
            .multilineTextAlignment(.center)
            .lineSpacing(CGFloat(fontSize) * 0.6)
            .frame(maxWidth: .infinity)
            .environment(\.layoutDirection, .rightToLeft)
    }

    private func buildArabicText() -> AttributedString {
        var result = AttributedString()

        for (index, ayah) in ayahs.enumerated() {
            // Strip any existing end-of-ayah markers from API text
            let cleanText = ayah.arabicText
                .replacingOccurrences(of: "\u{06DD}", with: "")
                .trimmingCharacters(in: .whitespaces)

            var ayahText = AttributedString(cleanText)
            ayahText.foregroundColor = .textPrimary
            result.append(ayahText)

            // Ornate parentheses ﴿ number ﴾ as ayah separator
            let numberStr = " \u{FD3F}\(ayah.numberInSurah.arabicNumeral)\u{FD3E} "
            var marker = AttributedString(numberStr)
            marker.foregroundColor = .appPrimary
            result.append(marker)

            if index < ayahs.count - 1 {
                result.append(AttributedString(" "))
            }
        }

        return result
    }
}

// MARK: - Arabic Numeral Conversion
private extension Int {
    var arabicNumeral: String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "ar")
        return formatter.string(from: NSNumber(value: self)) ?? "\(self)"
    }
}
