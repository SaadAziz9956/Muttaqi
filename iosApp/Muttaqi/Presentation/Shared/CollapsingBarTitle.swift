import SwiftUI

extension View {
    /// Shows `title` centred in the navigation bar once the page's own large title has scrolled up under the bar,
    /// like a native large title collapsing, but for the app's centred custom titles.
    /// - Parameter titleBottom: the large title's bottom edge in global coordinates, reported by the page as it scrolls
    func collapsingBarTitle(_ title: String, titleBottom: CGFloat) -> some View {
        modifier(CollapsingBarTitle(title: title, titleBottom: titleBottom))
    }
}

private struct CollapsingBarTitle: ViewModifier {
    let title: String
    let titleBottom: CGFloat

    // Apply to the view directly below the bar, so its top edge is where the bar ends
    @State private var barBottom: CGFloat = 0

    private var isShown: Bool { titleBottom < barBottom }

    func body(content: Content) -> some View {
        content
            .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).minY } action: { barBottom = $0 }
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text(title)
                        .font(.titleMedium)
                        .foregroundStyle(.appPrimary)
                        .lineLimit(1)
                        .opacity(isShown ? 1 : 0)
                        .animation(.easeInOut(duration: 0.2), value: isShown)
                }
            }
    }
}
