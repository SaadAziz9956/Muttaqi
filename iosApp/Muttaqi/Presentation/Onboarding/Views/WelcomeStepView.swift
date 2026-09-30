import Shared
import SwiftUI

struct WelcomeStepView: View {
    let onBegin: () -> Void
    
    var body: some View {
        VStack {
            Spacer()
            
            VStack(spacing: 8) {
                Text(OnboardingVerses.shared.basmala.arabic)
                    .font(.arabic(32))
                    .foregroundStyle(.textPrimary)
                
                Text(OnboardingVerses.shared.basmala.translation)
                    .font(.bodyMedium)
                    .foregroundStyle(.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            .offset(y: -60)
            
            Spacer()
            
            Button(action: onBegin) {
                Text("Begin")
                    .font(.bodySmall)
                    .foregroundStyle(.onPrimaryButton)
                    .frame(width: 140, height: 47)
                    .background(.primaryButton)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.bottom, 35)
        }
    }
}
