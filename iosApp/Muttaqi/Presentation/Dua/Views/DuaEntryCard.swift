import Shared
import SwiftUI

struct DuaEntryCard: View {
    let entry: DuaEntry
    let onShare: () -> Void
    let onCopy: () -> Void

    private var arabicText: AttributedString {
        .arabic(entry.arabic, size: 20)
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

                Button(action: onShare) {
                    Image("export-arrow-01-linear")
                        .resizable()
                        .frame(width: 18, height: 18)
                        .foregroundStyle(.textSecondary)
                        .padding(6)
                        .contentShape(.rect)
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityLabel("Share")

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

            if let grade = entry.grade {
                let gradeStyle = TranslationStyle(for: grade, size: 11)
                Text(grade)
                    .font(gradeStyle.font)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(gradeStyle.isRightToLeft ? .trailing : .leading)
                    .frame(maxWidth: .infinity, alignment: gradeStyle.isRightToLeft ? .trailing : .leading)
                    .padding(.top, 6)
            }

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
        .padding(.horizontal, 20)
        .padding(.top, 18)
        .padding(.bottom, 18)
        .softCard(cornerRadius: 26)
        .contextMenu {
            Button("Copy", systemImage: "doc.on.doc", action: onCopy)
            Button("Share", systemImage: "square.and.arrow.up", action: onShare)
        }
    }
}
