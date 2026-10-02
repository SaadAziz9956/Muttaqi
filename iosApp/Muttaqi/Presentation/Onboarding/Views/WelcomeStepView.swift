import Shared
import SwiftUI

struct WelcomeStepView: View {
    let onBegin: () -> Void

    var body: some View {
        let verse = OnboardingVerses.shared.basmala

        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 14) {
                Text(verse.arabic)
                    .font(.arabic(32))
                    .foregroundStyle(.textPrimary)

                Text(verse.translation)
                    .font(.custom("ReemKufi-Regular", size: 15, relativeTo: .subheadline))
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 36)
            .frame(maxWidth: .infinity)
            .softCard(artwork: .dawn)
            .padding(.horizontal, 20)

            Spacer()

            SoftButton(title: "Begin", action: onBegin)
                .padding(.bottom, 40)
        }
    }
}
