import SwiftUI

struct NameStepView: View {
    @Binding var name: String
    let onSave: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 22) {
                Text("What should we call you?")
                    .font(.custom("ReemKufi-Medium", size: 22, relativeTo: .title3))
                    .foregroundStyle(.appPrimary)

                SoftTextField(placeholder: "Type here...", text: $name, onSubmit: onSave)
            }
            .padding(.horizontal, 24)

            Spacer()

            SoftButton(title: "Save", action: onSave)
                .padding(.bottom, 40)
        }
    }
}
