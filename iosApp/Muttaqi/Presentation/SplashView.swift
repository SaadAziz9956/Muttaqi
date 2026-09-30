import SwiftUI

struct SplashView: View {
    let isOnboardingComplete: Bool

    var body: some View {
        ZStack {
            (isOnboardingComplete ? Color.splashBackground : Color(.systemBackground))
                .ignoresSafeArea()

            VStack(spacing: 22) {
                Text("متقي")
                    .font(.custom("ReemKufi-Regular", size: 60))
                    // The brand splash is deep green in both appearances, so its logo is always white
                    .foregroundStyle(isOnboardingComplete ? Color.white : Color.appPrimary)

                if !isOnboardingComplete {
                    ProgressView()
                        .tint(.appPrimary)
                }
            }
            .offset(y: -40)
        }
    }
}
