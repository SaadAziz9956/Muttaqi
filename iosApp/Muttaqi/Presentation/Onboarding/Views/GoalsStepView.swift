import Shared
import SwiftUI

struct GoalsStepView: View {
    let onBegin: () -> Void
    
    var body: some View {
        VStack {
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
            
            let verse = OnboardingVerses.shared.lovesThePure
            VStack(spacing: 5) {
                Text(verse.arabic)
                    .font(.arabic(26))
                    .foregroundStyle(.textPrimary)
                
                Text(verse.translation)
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
                
                Text(verse.source ?? "")
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
