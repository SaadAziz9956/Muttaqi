import Shared
import SwiftUI

struct SurahHeaderView: View {
    let surah: Surah?
    let previousSurah: Surah?
    let nextSurah: Surah?
    let onPrevious: () -> Void
    let onNext: () -> Void
    let onExplanation: () -> Void
    var onTitleBottomChange: (CGFloat) -> Void = { _ in }

    var body: some View {
        VStack(spacing: 0) {
            if let surah {
                Text(surah.name)
                    .font(.arabic(40))
                    .foregroundStyle(.textPrimary)
            }

            Text(surah?.englishName ?? "")
                .font(.custom("ReemKufi-Medium", size: 26))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { onTitleBottomChange($0) }
                .onDisappear { onTitleBottomChange(-.infinity) }
                .padding(.top, 6)

            Text(subtitle)
                .font(.custom("ReemKufi-Regular", size: 13))
                .foregroundStyle(.textSecondary)
                .padding(.top, 2)

            GlassEffectContainer(spacing: 4) {
                HStack(spacing: 8) {
                    if let previousSurah {
                        neighbourButton(previousSurah, isNext: false, action: onPrevious)
                    }
                    Button(action: onExplanation) {
                        Text("Explanation")
                            .font(.custom("ReemKufi-Medium", size: 13))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 18)
                            .frame(height: 36)
                            .softGlass(in: Capsule(), fill: .shareCard, rim: false)
                    }
                    .buttonStyle(SoftPressStyle())
                    if let nextSurah {
                        neighbourButton(nextSurah, isNext: true, action: onNext)
                    }
                }
            }
            .padding(.top, 18)
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.top, 20)
    }

    private var subtitle: String {
        guard let surah else { return "" }
        return "\(surah.englishNameTranslation) · \(surah.revelationType) · \(surah.numberOfAyahs) ayahs"
    }

    private func neighbourButton(_ neighbour: Surah, isNext: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if !isNext { chevron("arrow-left-02-linear") }
                Text(neighbour.englishName)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                if isNext { chevron("arrow-right-02-linear") }
            }
            .font(.custom("ReemKufi-Regular", size: 12))
            .foregroundStyle(.appPrimary)
            .padding(.horizontal, 12)
            .frame(height: 36)
            .softGlass(in: Capsule())
        }
        .buttonStyle(SoftPressStyle())
        .accessibilityLabel("\(isNext ? "Next" : "Previous") surah, \(neighbour.englishName)")
    }

    private func chevron(_ name: String) -> some View {
        Image(name)
            .resizable()
            .frame(width: 14, height: 14)
    }
}
