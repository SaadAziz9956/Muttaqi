import SwiftUI

struct AyahOfTheDayCard: View {
    let dailyAyah: DailyAyah
    let onOpen: () -> Void

    private var shareText: String {
        [dailyAyah.ayah.arabicText, dailyAyah.ayah.translation, "Quran (\(dailyAyah.reference))"]
            .compactMap { $0 }
            .joined(separator: "\n\n")
    }

    var body: some View {
        Button(action: onOpen) {
            VStack(spacing: 0) {
                cardTitle("Ayah of the Day")

                Text(dailyAyah.ayah.arabicText.kfgqpcEncoded)
                    .font(.arabic(21))
                    .lineSpacing(10)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .environment(\.layoutDirection, .rightToLeft)
                    .padding(.top, 22)

                if let translation = dailyAyah.ayah.translation {
                    Text(translation)
                        .font(TranslationStyle(for: translation, size: 14).font)
                        .foregroundStyle(.textPrimary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)
                        .padding(.top, 12)
                }

                Text("Quran (\(dailyAyah.reference))")
                    .font(.labelSmall)
                    .foregroundStyle(.brandTeal)
                    .padding(.top, 12)
            }
            .frame(maxWidth: .infinity)
            .cardStyle()
        }
        .buttonStyle(.plain)
        .contextMenu {
            Button("Copy", systemImage: "doc.on.doc") { UIPasteboard.general.string = shareText }
            ShareLink(item: shareText)
        }
        .accessibilityHint("Opens the ayah in the Quran")
    }
}

struct DuaOfTheDayCard: View {
    let dua: Dua

    private var shareText: String {
        [dua.arabic, dua.transliteration, dua.translation, "Quran (\(dua.id))"].joined(separator: "\n\n")
    }

    var body: some View {
        let translationStyle = TranslationStyle(for: dua.translation, size: 14)

        VStack(alignment: .leading, spacing: 0) {
            cardTitle("Dua of the Day")
                .frame(maxWidth: .infinity)

            Text(AttributedString.arabic(dua.arabic, size: 20))
                .lineSpacing(10)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 22)

            Text(dua.transliteration)
                .font(.custom("ReemKufi-Regular", size: 14))
                .foregroundStyle(.appPrimary)
                .padding(.top, 14)

            Text(dua.translation)
                .font(translationStyle.font)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(translationStyle.isRightToLeft ? .trailing : .leading)
                .frame(maxWidth: .infinity, alignment: translationStyle.isRightToLeft ? .trailing : .leading)
                .lineSpacing(4)
                .padding(.top, 12)

            Text("Quran (\(dua.id))")
                .font(.labelSmall)
                .foregroundStyle(.brandTeal)
                .frame(maxWidth: .infinity)
                .padding(.top, 14)
        }
        .cardStyle()
        .contextMenu {
            Button("Copy", systemImage: "doc.on.doc") { UIPasteboard.general.string = shareText }
            ShareLink(item: shareText)
        }
    }
}

private func cardTitle(_ title: String) -> some View {
    Text(title)
        .font(.custom("ReemKufi-Regular", size: 12))
        .foregroundStyle(.appPrimary)
}
