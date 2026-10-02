import SwiftUI

struct NameStepView: View {
    @Binding var name: String
    let onSave: () -> Void
    
    var body: some View {
        VStack {
            Spacer()
            
            VStack(spacing: 14) {
                Text("What should we call you?")
                    .font(.bodyLarge)
                    .foregroundStyle(.textPrimary)
                
                TextField("", text: $name, prompt: Text("Type here...")
                    .foregroundStyle(.textSecondary)
                )
                .font(.bodyMedium)
                .multilineTextAlignment(.center)
                .frame(height: 47)
                .background(.textField)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 24)
            }
            .offset(y: -60)
            
            Spacer()
            
            Button(action: onSave) {
                Text("Save")
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
