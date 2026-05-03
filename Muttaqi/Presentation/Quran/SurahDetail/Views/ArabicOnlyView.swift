import SwiftUI

struct ArabicOnlyView: View {
    let ayahs: [Ayah]
    let fontSize: FontSize

    var body: some View {
        ForEach(ayahs) { ayah in
            Text(arabicText(for: ayah))
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 40)

            Rectangle()
                .fill(Color(.systemGray5))
                .frame(height: 1)
                .padding(.top, 12)
        }
    }

    private func arabicText(for ayah: Ayah) -> AttributedString {
        let clean = ayah.arabicText
            .replacingOccurrences(of: "\u{06DD}", with: "")
            .trimmingCharacters(in: .whitespaces)

        var text = AttributedString(clean + " ")
        text.font = .arabic(fontSize.arabicSize)
        text.foregroundColor = .textPrimary

        var openParen = AttributedString("\u{FD3F}")
        openParen.font = .arabic(10)
        openParen.foregroundColor = .appPrimary

        var num = AttributedString(ayah.numberInSurah.arabicNumeral)
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
}

private extension Int {
    var arabicNumeral: String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "ar")
        return formatter.string(from: NSNumber(value: self)) ?? "\(self)"
    }
}
