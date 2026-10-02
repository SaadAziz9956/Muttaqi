import SwiftUI

struct SplashView: View {
    let isOnboardingComplete: Bool

    var body: some View {
        VStack(spacing: 22) {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)

            if !isOnboardingComplete {
                ProgressView()
                    .tint(.appPrimary)
            }
        }
        .offset(y: -40)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background { SoftBackdrop() }
    }
}
