import Shared
import SwiftUI

struct AyahOfTheDayCard: View {
    let dailyAyah: DailyAyah
    let dispatch: (HomeIntent) -> Void

    var body: some View {
        Button { dispatch(HomeIntentAyahOfTheDayTapped.shared) } label: {
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
            .dailyCard()
        }
        .buttonStyle(SoftPressStyle())
        .overlay(alignment: .topTrailing) {
            ShareButton { dispatch(HomeIntentShareTapped(card: .ayah)) }
                .padding(10)
        }
        .contextMenu { cardMenu(.ayah, dispatch: dispatch) }
        .accessibilityHint("Opens the ayah in the Quran")
    }
}

struct DuaOfTheDayCard: View {
    let dua: QuranicDua
    let dispatch: (HomeIntent) -> Void

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

            Text("Quran (\(dua.reference))")
                .font(.labelSmall)
                .foregroundStyle(.brandTeal)
                .frame(maxWidth: .infinity)
                .padding(.top, 14)
        }
        .dailyCard()
        .overlay(alignment: .topTrailing) {
            ShareButton { dispatch(HomeIntentShareTapped(card: .dua)) }
                .padding(10)
        }
        .contextMenu { cardMenu(.dua, dispatch: dispatch) }
    }
}

/// A short authentic hadith from Explore, in full as HadeethEnc publishes it
struct HadithOfTheDayCard: View {
    let hadith: HadithPassage
    let dispatch: (HomeIntent) -> Void

    var body: some View {
        let style = TranslationStyle(for: hadith.translation, size: 14)
        let source = hadith.source

        VStack(spacing: 0) {
            cardTitle("Hadith of the Day")

            if !hadith.arabic.isEmpty {
                Text(AttributedString.arabic(hadith.arabic, size: 19))
                    .lineSpacing(9)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 22)
            }

            Text(hadith.translation)
                .font(style.font)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .lineSpacing(style.isRightToLeft ? 8 : 4)
                .padding(.top, 12)

            Text(source)
                .font(TranslationStyle.isArabicScript(source) ? TranslationStyle(for: source, size: 11).font : .labelSmall)
                .foregroundStyle(.brandTeal)
                .multilineTextAlignment(.center)
                .padding(.top, 12)
        }
        .frame(maxWidth: .infinity)
        .dailyCard()
        .overlay(alignment: .topTrailing) {
            ShareButton { dispatch(HomeIntentShareTapped(card: .hadith)) }
                .padding(10)
        }
        .contextMenu { cardMenu(.hadith, dispatch: dispatch) }
    }
}

/// Opens the Share page for the card it sits on
private struct ShareButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image("export-arrow-01-linear")
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.textSecondary)
                .padding(6)
                .contentShape(.rect)
        }
        .buttonStyle(SoftPressStyle())
        .accessibilityLabel("Share")
    }
}

@ViewBuilder
private func cardMenu(_ card: DailyCard, dispatch: @escaping (HomeIntent) -> Void) -> some View {
    Button("Copy", systemImage: "doc.on.doc") { dispatch(HomeIntentCopyTapped(card: card)) }
    Button("Share", systemImage: "square.and.arrow.up") { dispatch(HomeIntentShareTapped(card: card)) }
}

private extension View {
    /// The daily cards float on the Home page like its tiles
    func dailyCard() -> some View {
        padding(.horizontal, 22)
            .padding(.top, 18)
            .padding(.bottom, 22)
            .softCard()
    }
}

private func cardTitle(_ title: String) -> some View {
    Text(title)
        .font(.custom("ReemKufi-Regular", size: 12))
        .foregroundStyle(.appPrimary)
}
