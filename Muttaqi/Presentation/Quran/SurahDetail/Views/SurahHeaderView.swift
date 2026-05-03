import SwiftUI

struct SurahHeaderView: View {
    let surah: Surah?
    let canGoNext: Bool
    let canGoPrevious: Bool
    let onPrevious: () -> Void
    let onNext: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onPrevious) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundStyle(canGoPrevious ? .textPrimary : .textSecondary.opacity(0.3))
                        .padding(.vertical, 16)
                        .padding(.horizontal, 20)
                        .contentShape(Rectangle())
                }
                .padding(.leading, 20)
                .disabled(!canGoPrevious)

                Spacer()

                VStack(spacing: 0) {
                    Text(surah?.englishName ?? "")
                        .font(.custom("ReemKufi-Regular", size: 28))
                        .foregroundStyle(.appPrimary)

                    Text(surah?.englishNameTranslation ?? "")
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                }

                Spacer()

                Button(action: onNext) {
                    Image(systemName: "chevron.right")
                        .font(.title2)
                        .foregroundStyle(canGoNext ? .textPrimary : .textSecondary.opacity(0.3))
                        .padding(.vertical, 16)
                        .padding(.horizontal, 20)
                        .contentShape(Rectangle())
                }
                .padding(.trailing, 20)
                .disabled(!canGoNext)
            }
            .padding(.top, 16)

            Button {
                // TODO: Navigate to explanation
            } label: {
                Text("Explanation")
                    .font(.labelSmall)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 30)
                    .padding(.vertical, 6)
                    .background(.primaryButton)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(.top, 12)
        }
    }
}
