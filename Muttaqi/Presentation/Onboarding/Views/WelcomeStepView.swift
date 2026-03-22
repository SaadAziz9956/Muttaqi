import SwiftUI

struct WelcomeStepView: View {
    let onBegin: () -> Void
    
    var body: some View {
        VStack {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)
                .padding(.top, 48)
            
            Spacer()
            
            VStack(spacing: 8) {
                Text("بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ")
                    .font(.arabic(32))
                    .foregroundStyle(.textPrimary)
                
                Text("In the name of Allah,\nthe most gracious, the most merciful")
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
