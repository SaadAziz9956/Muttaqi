import SwiftUI

struct SetupStepView: View {
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            VStack(spacing: 0) {
                Text("متقي")
                    .font(.custom("ReemKufi-Regular", size: 60))
                    .foregroundStyle(.appPrimary)
                
                Text("Setting up for first time")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                    .padding(.top, 22)
                
                ProgressView()
                    .tint(.appPrimary)
                    .padding(.top, 12)
            }
            
            Spacer()
        }
    }
}
