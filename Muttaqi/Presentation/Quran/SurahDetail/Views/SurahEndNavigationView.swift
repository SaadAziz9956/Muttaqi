import SwiftUI

// Shown after the last ayah, where a reader who has finished the surah naturally moves on
struct SurahEndNavigationView: View {
    let previousSurah: Surah?
    let nextSurah: Surah?
    let onPrevious: () -> Void
    let onNext: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            if let previousSurah {
                card(previousSurah, isNext: false, action: onPrevious)
            } else {
                Color.clear.frame(maxWidth: .infinity)
            }

            if let nextSurah {
                card(nextSurah, isNext: true, action: onNext)
            } else {
                Color.clear.frame(maxWidth: .infinity)
            }
        }
    }

    private func card(_ neighbour: Surah, isNext: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(alignment: isNext ? .trailing : .leading, spacing: 4) {
                Text(isNext ? "Next" : "Previous")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)

                HStack(spacing: 4) {
                    if !isNext { Image(systemName: "chevron.left") }
                    Text(neighbour.englishName)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                    if isNext { Image(systemName: "chevron.right") }
                }
                .font(.bodyMedium)
                .foregroundStyle(.textPrimary)
            }
            .padding(15)
            .frame(maxWidth: .infinity, alignment: isNext ? .trailing : .leading)
            .background(.surahContainer)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(isNext ? "Next" : "Previous") surah, \(neighbour.englishName)")
    }
}
