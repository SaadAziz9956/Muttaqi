import SwiftUI

struct AyahCardView: View {
    let ayah: Ayah
    let fontSize: Int

    private var cleanArabicText: String {
        ayah.arabicText
            .replacingOccurrences(of: "\u{06DD}", with: "")
            .trimmingCharacters(in: .whitespaces)
    }

    private var attributedAyah: AttributedString {
        var text = AttributedString(cleanArabicText + " ")
        text.font = .arabic(CGFloat(fontSize))
        text.foregroundColor = .textPrimary

        var openParen = AttributedString("\u{FD3F}")
        openParen.font = .arabic(10)
        openParen.foregroundColor = .appPrimary

        var num = AttributedString("\(ayah.numberInSurah.arabicNumeral)")
        num.font = .arabic(14)
        num.foregroundColor = .appPrimary

        var closeParen = AttributedString("\u{FD3E}")
        closeParen.font = .arabic(10)
        closeParen.foregroundColor = .appPrimary

        text.append(openParen)
        text.append(num)
        text.append(closeParen)
        return text
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(attributedAyah)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 40)

            if let transliteration = ayah.transliteration, !transliteration.isEmpty {
                Text(transliteration)
                    .font(.bodyMedium)
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 12)
            }

            if let translation = ayah.translation, !translation.isEmpty {
                Text("\(ayah.numberInSurah).  \(translation)")
                    .font(.bodySmall)
                    .foregroundStyle(.textPrimary)
                    .padding(.top, 8)
            }

            Rectangle()
                .fill(Color(.systemGray5))
                .frame(height: 1)
                .padding(.top, 12)
        }
    }
}

private extension Int {
    var arabicNumeral: String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "ar")
        return formatter.string(from: NSNumber(value: self)) ?? "\(self)"
    }
}
