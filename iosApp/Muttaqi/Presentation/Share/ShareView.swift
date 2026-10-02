import Shared
import SwiftUI

struct ShareView: View {
    @State private var screen: SharedViewModel<ShareViewModel, ShareState>
    @State private var image: UIImage?
    @State private var titleBottom: CGFloat = .infinity
    @Environment(\.dismiss) private var dismiss

    init(passage: SharePassage) {
        _screen = State(initialValue: SharedViewModel(ShareViewModels.shared.share(passage: passage)) { $0.state })
    }

    private var passage: SharePassage { screen.state.passage }

    var body: some View {
        let verse = screen.state.verse

        ScrollView {
            VStack(spacing: 0) {
                Text("Share")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .padding(.top, 24)

                Text(verse.text.quoted)
                    .font(TranslationStyle(for: verse.text, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text(verse.source)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)

                ShareCard(passage: passage)
                    .padding(.top, 32)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Share", titleBottom: titleBottom)
        .navigationBarBackButtonHidden(true)
        .background(SwipeBackEnabler())
        .toolbar(.hidden, for: .tabBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button { dismiss() } label: {
                    Image("arrow-left-02-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.textPrimary)
                }
                .accessibilityLabel("Back")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                if let image {
                    let picture = Image(uiImage: image)
                    ShareLink(item: picture, preview: SharePreview(passage.reference, image: picture)) {
                        Image("send-2-linear")
                            .resizable()
                            .frame(width: 22, height: 22)
                            .foregroundStyle(.textPrimary)
                    }
                    .accessibilityLabel("Share")
                }
            }
        }
        .task { image = ShareCard.image(of: passage) }
    }
}
