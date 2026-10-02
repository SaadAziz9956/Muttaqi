import SwiftUI

struct SetupStepView: View {
    let errorMessage: String?
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)

            VStack(spacing: 0) {
                if let errorMessage {
                    Text("Setup Failed")
                        .font(.custom("ReemKufi-Medium", size: 20, relativeTo: .title3))
                        .foregroundStyle(.appPrimary)

                    Text(errorMessage)
                        .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .subheadline))
                        .foregroundStyle(.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)
                } else {
                    Text("Setting up for first time")
                        .font(.custom("ReemKufi-Medium", size: 20, relativeTo: .title3))
                        .foregroundStyle(.appPrimary)

                    Text("Downloading Quran data...")
                        .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .subheadline))
                        .foregroundStyle(.textSecondary)
                        .padding(.top, 8)

                    ProgressView()
                        .tint(.appPrimary)
                        .padding(.top, 16)
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 28)
            .frame(maxWidth: .infinity)
            .softCard()
            .padding(.horizontal, 20)
            .padding(.top, 28)

            if errorMessage != nil {
                SoftButton(title: "Retry", action: onRetry)
                    .padding(.top, 28)
            }

            Spacer()
        }
        .animation(.smooth, value: errorMessage)
    }
}
