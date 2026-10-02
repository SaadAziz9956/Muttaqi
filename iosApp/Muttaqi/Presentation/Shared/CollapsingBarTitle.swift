import SwiftUI

extension View {
    func collapsingBarTitle(_ title: String, titleBottom: CGFloat) -> some View {
        modifier(CollapsingBarTitle(title: title, titleBottom: titleBottom))
    }
}

private struct CollapsingBarTitle: ViewModifier {
    let title: String
    let titleBottom: CGFloat

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
