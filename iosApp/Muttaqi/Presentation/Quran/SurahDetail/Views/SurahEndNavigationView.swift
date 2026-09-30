import Shared
import SwiftUI

// Shown after the last ayah, where a reader who has finished the surah naturally moves on
struct SurahEndNavigationView: View {
    let previousSurah: Shared.Surah?
    let nextSurah: Shared.Surah?
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

    private func card(_ neighbour: Shared.Surah, isNext: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(alignment: isNext ? .trailing : .leading, spacing: 4) {
                Text(isNext ? "Next" : "Previous")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)

                HStack(spacing: 4) {
                    if !isNext { chevron("arrow-left-02-linear") }
                    Text(neighbour.englishName)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                    if isNext { chevron("arrow-right-02-linear") }
                }
                .font(.bodyMedium)
                .foregroundStyle(.textPrimary)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: isNext ? .trailing : .leading)
            .softCard(cornerRadius: 24, glass: true)
        }
        .buttonStyle(SoftPressStyle())
        .accessibilityLabel("\(isNext ? "Next" : "Previous") surah, \(neighbour.englishName)")
    }

    private func chevron(_ name: String) -> some View {
        Image(name)
            .resizable()
            .frame(width: 16, height: 16)
    }
}
