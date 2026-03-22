import SwiftUI

struct NameStepView: View {
    @State private var name: String = ""
    let onSave: (String) -> Void
    
    var body: some View {
        VStack {
            Text("متقي")
                .font(.custom("ReemKufi-Regular", size: 60))
                .foregroundStyle(.appPrimary)
                .padding(.top, 48)
            
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
            
            Button {
                guard !name.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                onSave(name)
            } label: {
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
