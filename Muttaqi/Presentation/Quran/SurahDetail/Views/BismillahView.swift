import SwiftUI

struct BismillahView: View {
    let text: String
    let translation: String
    
    var body: some View {
        VStack(spacing: 0) {
            Text(text)
                .font(.arabic(18))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 35)

            Text(translation)
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 18)
                .padding(.horizontal, 60)
                .padding(.bottom, 40)
        }
    }
}
