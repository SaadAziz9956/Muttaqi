import Shared
import SwiftUI

/// A preview of the card to share; the send button opens the share sheet with the card as an image
struct ShareView: View {
    @State private var screen: SharedViewModel<ShareViewModel, ShareState>
    @State private var image: UIImage?
    @Environment(\.dismiss) private var dismiss

    init(passage: SharePassage) {
        _screen = State(initialValue: SharedViewModel(ShareViewModels.shared.share(passage: Shared.SharePassage(passage))) { $0.state })
    }

    private var passage: Shared.SharePassage { screen.state.passage }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Share")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.white)
                    .padding(.top, 8)

                ShareCard(passage: passage)
                    .padding(.horizontal, 21)
                    .padding(.top, 28)
            }
            .padding(.bottom, 32)
        }
        .safeAreaInset(edge: .bottom) { footer }
        .background(Color.shareCard.ignoresSafeArea())
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .background(SwipeBackEnabler())
        .toolbar(.hidden, for: .tabBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button { dismiss() } label: {
                    Image("arrow-left-02-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.white)
                }
                .accessibilityLabel("Back")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                // The share sheet's own Save Image saves the card to Photos, so there's no separate save button
                if let image {
                    let picture = Image(uiImage: image)
                    ShareLink(item: picture, preview: SharePreview(passage.reference, image: picture)) {
                        Image("send-2-linear")
                            .resizable()
                            .frame(width: 24, height: 24)
                            .foregroundStyle(.white)
                    }
                    .accessibilityLabel("Share")
                }
            }
        }
        .task { image = ShareCard.image(of: passage) }
    }

    private var footer: some View {
        let verse = screen.state.verse

        return VStack(spacing: 4) {
            Text(verse.text)
                .font(TranslationStyle(for: verse.text, size: 14).font)
                .foregroundStyle(.white.opacity(0.85))
                .multilineTextAlignment(.center)
            Text(verse.source)
                .font(.labelSmall)
                .foregroundStyle(.white.opacity(0.85))
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity)
        .background(Color.shareCard)
    }
}

/// Opens the Share page for a passage; used on every card and page that shows one
struct ShareButton: View {
    let passage: SharePassage
    var size: CGFloat = 18
    var color: Color = .textSecondary
    /// Enlarges the tap area around a small icon; none in a toolbar, which has its own
    var padding: CGFloat = 6

    var body: some View {
        NavigationLink(value: passage) {
            Image("export-arrow-01-linear")
                .resizable()
                .frame(width: size, height: size)
                .foregroundStyle(color)
                .padding(padding)
                .contentShape(.rect)
        }
        .buttonStyle(SoftPressStyle())
        .accessibilityLabel("Share")
    }
}
