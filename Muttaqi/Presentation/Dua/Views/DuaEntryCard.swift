import SwiftUI

struct DuaEntryCard: View {
    let entry: DuaEntry

    private var arabicText: AttributedString {
        .arabic(entry.arabic, size: 20)
    }

    private var shareText: String {
        [entry.arabic, entry.transliteration, entry.translation, entry.source]
            .filter { !$0.isEmpty }
            .joined(separator: "\n\n")
    }

    var body: some View {
        let translationStyle = TranslationStyle(for: entry.translation, size: 14)

        VStack(alignment: .leading, spacing: 0) {
            Text(arabicText)
                .lineSpacing(10)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 8)

            if !entry.transliteration.isEmpty {
                Text(entry.transliteration)
                    .font(.custom("ReemKufi-Regular", size: 14))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 14)
            }

            Text(entry.translation)
                .font(translationStyle.font)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(translationStyle.isRightToLeft ? .trailing : .leading)
                .frame(maxWidth: .infinity, alignment: translationStyle.isRightToLeft ? .trailing : .leading)
                .lineSpacing(4)
                .padding(.top, 12)

            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text(entry.source)
                    .font(.labelSmall)
                    .foregroundStyle(.brandTeal)

                Spacer(minLength: 8)

                if entry.repeatCount > 1 {
                    Text("\(entry.repeatCount)×")
                        .font(.custom("ReemKufi-Medium", size: 12))
                        .foregroundStyle(.brandTeal)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 3)
                        .background(.tintedSurface, in: .capsule)
                        .accessibilityLabel("Repeat \(entry.repeatCount) times")
                }
            }
            .padding(.top, 16)

            // The book's own reference, e.g. volume and page in Bukhari with Fath al-Bari
            if !entry.reference.isEmpty {
                Text(entry.reference)
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.trailing)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .environment(\.layoutDirection, .rightToLeft)
                    .padding(.top, 6)
            }
        }
        .cardStyle()
        .contextMenu {
            Button("Copy", systemImage: "doc.on.doc") { UIPasteboard.general.string = shareText }
            ShareLink(item: shareText)
        }
    }
}
