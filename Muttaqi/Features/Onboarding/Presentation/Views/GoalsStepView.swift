import SwiftUI

struct GoalsStepView: View {
    let onBegin: () -> Void
    
    var body: some View {
        VStack {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)
                .padding(.top, 48)
            
            Spacer()
            
            VStack(alignment: .leading, spacing: 0) {
                Text("We will help you to achieve your Muslim Goals")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                Text("by using our App on the daily basis you will")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 8)
                
                VStack(alignment: .leading, spacing: 8) {
                    GoalItem(text: "Become a better Muslim")
                    GoalItem(text: "Read Quran with translation")
                    GoalItem(text: "Zikr o Azkar")
                    GoalItem(text: "Learn Sunnah")
                }
                .padding(.top, 26)
                
                Text("In Shaa Allah")
                    .font(.bodyMedium)
                    .foregroundStyle(.appPrimary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.top, 6)
            }
            .padding(.horizontal, 16)
            
            Button(action: onBegin) {
                Text("Begin")
                    .font(.bodySmall)
                    .foregroundStyle(.onPrimaryButton)
                    .frame(width: 140, height: 47)
                    .background(.primaryButton)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.top, 58)
            
            Spacer()
            
            VStack(spacing: 5) {
                Text("وَاللَّهُ يُحِبُّ الْمُطَّهِّرِينَ")
                    .font(.arabic(26))
                    .foregroundStyle(.textPrimary)
                
                Text("Allah loves those who keep themselves pure.")
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
                
                Text("Quran (9:108)")
                    .font(.labelMedium)
                    .foregroundStyle(.textSecondary)
            }
            .padding(.bottom, 35)
        }
    }
}

private struct GoalItem: View {
    let text: String
    
    var body: some View {
        HStack(spacing: 8) {
            Circle()
                .fill(.textMediumGrey)
                .frame(width: 4, height: 4)
            Text(text)
                .font(.labelSmall)
                .foregroundStyle(.textMediumGrey)
        }
    }
}
