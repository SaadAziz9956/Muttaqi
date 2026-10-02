import Shared
import SwiftUI

struct GoalsStepView: View {
    let onBegin: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 16)

            VStack(alignment: .leading, spacing: 0) {
                Text("We will help you to achieve your Muslim Goals")
                    .font(.custom("ReemKufi-Medium", size: 20, relativeTo: .title3))
                    .foregroundStyle(.appPrimary)

                Text("by using our App on the daily basis you will")
                    .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .subheadline))
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 6)

                VStack(alignment: .leading, spacing: 12) {
                    GoalRow(icon: "lovely-linear", text: "Become a better Muslim")
                    GoalRow(icon: "book-open-linear", text: "Read Quran with translation")
                    GoalRow(icon: "repeat-circle-linear", text: "Zikr o Azkar")
                    GoalRow(icon: "lamp-on-linear", text: "Learn Sunnah")
                }
                .padding(.top, 20)
            }
            .padding(22)
            .frame(maxWidth: .infinity, alignment: .leading)
            .softCard()
            .padding(.horizontal, 20)

            Text("In Shaa Allah")
                .font(.custom("ReemKufi-Medium", size: 16, relativeTo: .headline))
                .foregroundStyle(.appPrimary)
                .padding(.top, 20)

            SoftButton(title: "Begin", action: onBegin)
                .padding(.top, 22)

            Spacer()

            let verse = OnboardingVerses.shared.lovesThePure
            VStack(spacing: 6) {
                Text(verse.arabic)
                    .font(.arabic(26))
                    .foregroundStyle(.textPrimary)

                Text(verse.translation)
                    .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .subheadline))
                    .foregroundStyle(.textPrimary)

                Text(verse.source ?? "")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
            }
            .multilineTextAlignment(.center)
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}

private struct GoalRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 12) {
            Image(icon)
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(.brandTeal)
                .frame(width: 36, height: 36)
                .background(.tintedSurface, in: .circle)
                .accessibilityHidden(true)

            Text(text)
                .font(.custom("ReemKufi-Regular", size: 15, relativeTo: .subheadline))
                .foregroundStyle(.textPrimary)
        }
    }
}
