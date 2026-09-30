import Shared
import SwiftUI

struct SurahCardView: View {
    let surah: Surah

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top) {
                Text(Int(surah.number), format: .number)
                    .font(.custom("ReemKufi-Medium", size: 13))
                    .foregroundStyle(.appPrimary)
                    .frame(width: 32, height: 32)
                    .background(.tintedSurface, in: .circle)
                Spacer(minLength: 6)
                Text(surah.name)
                    .font(.arabic(18))
                    .foregroundStyle(.textPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }

            Text(surah.englishName)
                .font(.custom("ReemKufi-Medium", size: 16))
                .foregroundStyle(.appPrimary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .padding(.top, 16)

            Text(surah.englishNameTranslation)
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .lineLimit(1)
                .minimumScaleFactor(0.85)

            Text("\(surah.numberOfAyahs) ayahs · \(surah.revelationType)")
                .font(.custom("ReemKufi-Regular", size: 11))
                .foregroundStyle(.brandTeal)
                .padding(.top, 10)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .softCard(cornerRadius: 24)
        .accessibilityElement(children: .combine)
    }
}
