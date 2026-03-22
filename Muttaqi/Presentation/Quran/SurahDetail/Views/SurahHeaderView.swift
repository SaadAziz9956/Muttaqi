import SwiftUI

/// Extracted header component for better separation of concerns
struct SurahHeaderView: View {
    let content: SurahContentViewModel.SurahContent?
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
                }
                .padding(.leading, 40)
                .disabled(!canGoPrevious)

                Spacer()

                VStack(spacing: 0) {
                    Text(content?.surah.englishName ?? "")
                        .font(.custom("ReemKufi-Regular", size: 28))
                        .foregroundStyle(.appPrimary)

                    Text(content?.surah.englishNameTranslation ?? "")
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                }

                Spacer()

                Button(action: onNext) {
                    Image(systemName: "chevron.right")
                        .font(.title2)
                        .foregroundStyle(canGoNext ? .textPrimary : .textSecondary.opacity(0.3))
                }
                .padding(.trailing, 40)
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
