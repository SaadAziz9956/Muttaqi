import SwiftUI

/// White rounded card with the design's soft shadow, used by the Home and Dua cards
private struct CardStyle: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content
            .padding(.horizontal, 22)
            .padding(.top, 14)
            .padding(.bottom, 18)
            .background(.cardBackground, in: .rect(cornerRadius: 15))
            // The design's soft shadow reads on white; in dark mode the lighter card surface does that job instead
            .shadow(color: .black.opacity(colorScheme == .dark ? 0 : 0.25), radius: 2, y: 1)
    }
}

extension View {
    func cardStyle() -> some View {
        modifier(CardStyle())
    }
}
