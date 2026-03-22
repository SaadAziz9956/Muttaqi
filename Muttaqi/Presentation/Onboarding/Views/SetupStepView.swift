import SwiftUI

struct SetupStepView: View {
    let errorMessage: String?
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            if let errorMessage {
                errorContent(errorMessage)
            } else {
                loadingContent
            }

            Spacer()
        }
    }

    private var loadingContent: some View {
        VStack(spacing: 0) {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)

            Text("Setting up for first time")
                .font(.bodyLarge)
                .foregroundStyle(.textPrimary)
                .padding(.top, 22)

            Text("Downloading Quran data...")
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .padding(.top, 8)

            ProgressView()
                .tint(.appPrimary)
                .padding(.top, 12)
        }
    }

    private func errorContent(_ message: String) -> some View {
        VStack(spacing: 0) {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)

            Text("Setup Failed")
                .font(.bodyLarge)
                .foregroundStyle(.textPrimary)
                .padding(.top, 22)

            Text(message)
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .padding(.top, 8)

            Button(action: onRetry) {
                Text("Retry")
                    .font(.titleSmall)
                    .foregroundStyle(.white)
                    .frame(width: 160, height: 48)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
            .padding(.top, 24)
        }
    }
}
