import SwiftUI

struct NotificationStepView: View {
    let onEnable: () -> Void
    let onSkip: () -> Void
    
    var body: some View {
        VStack {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)
                .padding(.top, 48)
            
            Spacer()
            
            VStack(spacing: 12) {
                Text("Enable Notification")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                Text("Enable Notification so you don't miss\ndaily Quran ayah and Azkar and Namaz Alarms.")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
            }
            
            Spacer()
            
            VStack(spacing: 8) {
                Text("Would you like to turn on Notifications?")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                Button(action: onEnable) {
                    Text("Turn on")
                        .font(.labelSmall)
                        .foregroundStyle(.onPrimaryButton)
                        .frame(width: 100, height: 33)
                        .background(.primaryButton)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }.padding(.top, 17)
                
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
