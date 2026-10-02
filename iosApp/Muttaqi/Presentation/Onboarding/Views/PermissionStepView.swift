import SwiftUI

struct PermissionStepView: View {
    let icon: String
    let title: String
    let detail: String
    let question: String
    let action: String
    let onAction: () -> Void
    let onSkip: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 0) {
                SoftCircle(size: 72, filled: true) {
                    Image(icon)
                        .resizable()
                        .frame(width: 30, height: 30)
                }
                .accessibilityHidden(true)

                Text(title)
                    .font(.custom("ReemKufi-Medium", size: 22, relativeTo: .title3))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 22)

                Text(detail)
                    .font(.custom("ReemKufi-Regular", size: 15, relativeTo: .subheadline))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.top, 10)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 32)
            .frame(maxWidth: .infinity)
            .softCard()
            .padding(.horizontal, 20)

            Spacer()

            VStack(spacing: 14) {
                Text(question)
                    .font(.custom("ReemKufi-Medium", size: 17, relativeTo: .headline))
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)

                SoftButton(title: action, action: onAction)
                    .padding(.top, 4)

                SoftButton(title: "Not now", kind: .secondary, action: onSkip)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
    }
}
