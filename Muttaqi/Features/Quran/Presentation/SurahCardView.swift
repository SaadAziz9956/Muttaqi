import SwiftUI

struct SurahCardView: View {
    let surah: Surah
    
    var body: some View {
        VStack(spacing: 0) {
            Text(surah.englishName)
                .font(.bodyMedium)
                .foregroundStyle(.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(surah.englishNameTranslation)
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(surah.name)
                .font(.arabic(18))
                .foregroundStyle(.textPrimary)
                .padding(.top, 16)
            
            Text("1 - \(surah.numberOfAyahs)")
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .padding(.top, 18)
        }
        .padding(15)
        .frame(maxWidth: .infinity)
        .background(.surahContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
