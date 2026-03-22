import SwiftUI

struct SplashView: View {
    let isOnboardingComplete: Bool
    
    var body: some View {
        ZStack {
            (isOnboardingComplete ? .appPrimary : Color.white)
                .ignoresSafeArea()
            
            VStack(spacing: 22) {
                Text("متقي")
                    .font(.custom("ReemKufi-Regular", size: 60))
                    .foregroundStyle(isOnboardingComplete ? .onPrimary : .appPrimary)
                
                if !isOnboardingComplete {
                    ProgressView()
                        .tint(.appPrimary)
                }
            }
            .offset(y: -40)
        }
    }
}
