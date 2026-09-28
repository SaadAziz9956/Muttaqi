import SwiftUI

struct SurahHeaderView: View {
    let surah: Surah?
    let previousSurah: Surah?
    let nextSurah: Surah?
    let onPrevious: () -> Void
    let onNext: () -> Void
    let onExplanation: () -> Void
    /// Reports the title's bottom edge in global coordinates as the header scrolls
    var onTitleBottomChange: (CGFloat) -> Void = { _ in }

    var body: some View {
        VStack(spacing: 0) {
            VStack(spacing: 0) {
                Text(surah?.englishName ?? "")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { onTitleBottomChange($0) }
                    // Jumping to an ayah far down unloads the header without a last geometry update, so report it gone
                    .onDisappear { onTitleBottomChange(-.infinity) }

                Text(subtitle)
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
            }
            .padding(.top, 16)

            Button(action: onExplanation) {
                Text("Explanation")
                    .font(.labelSmall)
                    .foregroundStyle(.onPrimaryButton)
                    .padding(.horizontal, 30)
                    .padding(.vertical, 6)
                    .background(.primaryButton)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(.top, 12)

            // Surah switching is labelled with the neighbour's name so it can't be mistaken for the back button above
            HStack {
                if let previousSurah {
                    neighbourButton(previousSurah, isNext: false, action: onPrevious)
                }
                Spacer()
                if let nextSurah {
                    neighbourButton(nextSurah, isNext: true, action: onNext)
                }
            }
            .frame(height: 44)
            .padding(.horizontal, 4)
            .padding(.top, 4)
        }
    }

    private var subtitle: String {
        guard let surah else { return "" }
        return "\(surah.englishNameTranslation) · \(surah.number) of \(SurahNumber.maximum)"
    }

    private func neighbourButton(_ neighbour: Surah, isNext: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if !isNext { chevron("arrow-left-02-linear") }
                Text(neighbour.englishName)
                if isNext { chevron("arrow-right-02-linear") }
            }
            .font(.labelLarge)
            .foregroundStyle(.textSecondary)
            .frame(maxHeight: .infinity)
            .contentShape(Rectangle())
        }
        .accessibilityLabel("\(isNext ? "Next" : "Previous") surah, \(neighbour.englishName)")
    }

    private func chevron(_ name: String) -> some View {
        Image(name)
            .resizable()
            .frame(width: 16, height: 16)
    }
}
