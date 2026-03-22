import SwiftUI

struct LocationStepView: View {
    let onFind: () -> Void
    let onSkip: () -> Void
    
    var body: some View {
        VStack {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)
                .padding(.top, 48)
            
            Spacer()
            
            VStack(spacing: 12) {
                Text("Select Location")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                Text("Select your current location to get latest Namaz timing")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
            }
            
            Spacer()
            
            VStack(spacing: 8) {
                Text("Find your City")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                Button(action: onFind) {
                    Text("Find")
                        .font(.labelSmall)
                        .foregroundStyle(.onPrimaryButton)
                        .frame(width: 100, height: 33)
                        .background(.primaryButton)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .padding(.top, 17)
                
                Button(action: onSkip) {
                    Text("Not now")
                        .font(.labelSmall)
                        .foregroundStyle(.textSecondary)
                        .underline()
                }
            }
            .padding(.bottom, 35)
            
            Spacer()
        }
    }
}
