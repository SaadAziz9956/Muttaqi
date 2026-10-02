import SwiftUI

extension Color {
    fileprivate static func dynamic(light: UIColor, dark: UIColor) -> Color {
        Color(uiColor: UIColor { $0.userInterfaceStyle == .dark ? dark : light })
    }

    fileprivate init(hex: UInt32, alpha: CGFloat = 1) {
        self.init(uiColor: UIColor(hex: hex, alpha: alpha))
    }

    static let softCanvas = dynamic(light: UIColor(hex: 0xF3F7F5), dark: UIColor(hex: 0x0A1210))
    static let softSurface = dynamic(light: UIColor(white: 1, alpha: 0.82), dark: UIColor(hex: 0x14201C, alpha: 0.92))
    static let softRim = dynamic(light: UIColor(white: 1, alpha: 0.95), dark: UIColor(white: 1, alpha: 0.07))
    static let softShadow = dynamic(light: UIColor(hex: 0x114538, alpha: 0.10), dark: UIColor(white: 0, alpha: 0.45))
}

extension UIColor {
    fileprivate convenience init(hex: UInt32, alpha: CGFloat = 1) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255,
            alpha: alpha
        )
    }
}

private struct SoftCard: ViewModifier {
    let cornerRadius: CGFloat
    let rim: CGFloat
    let glass: Bool
    let artwork: SoftArtwork.Palette?

    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        let card = content
            .background {
                ZStack {
                    shape.fill(Color.softSurface)
                        .shadow(color: .softShadow, radius: 22, y: 12)
                        .shadow(color: .softShadow.opacity(0.5), radius: 2, y: 1)
                    if let artwork {
                        SoftArtwork(palette: artwork).clipShape(shape)
                    }
                }
            }
            .overlay { shape.strokeBorder(Color.softRim, lineWidth: rim) }
            .contentShape(shape)
        if glass {
            card.glassEffect(.regular.tint(.softSurface).interactive(), in: shape)
        } else {
            card
        }
    }
}

extension View {
    func softCard(
        cornerRadius: CGFloat = 28,
        rim: CGFloat = 1.5,
        glass: Bool = false,
        artwork: SoftArtwork.Palette? = nil
    ) -> some View {
        modifier(SoftCard(cornerRadius: cornerRadius, rim: rim, glass: glass, artwork: artwork))
    }

    func softPill() -> some View {
        padding(.horizontal, 14)
            .frame(height: 34)
            .softGlass(in: .capsule)
    }

    func softGlass<S: InsettableShape>(in shape: S, fill: Color = .softSurface, rim: Bool = true) -> some View {
        background {
            shape.fill(fill).shadow(color: .softShadow, radius: 10, y: 5)
        }
        .overlay { if rim { shape.strokeBorder(Color.softRim, lineWidth: 1.5) } }
        .contentShape(shape)
        .glassEffect(.regular.tint(fill).interactive(), in: shape)
    }
}

struct SoftCircle<Label: View>: View {
    var size: CGFloat = 36
    var filled = false
    @ViewBuilder let label: Label

    var body: some View {
        label
            .frame(width: size, height: size)
            .foregroundStyle(filled ? Color.white : Color.appPrimary)
            .softGlass(in: Circle(), fill: filled ? .shareCard : .softSurface, rim: !filled)
    }
}

struct SoftButton: View {
    enum Kind {
        case primary
        case secondary
    }

    let title: String
    var kind: Kind = .primary
    let action: () -> Void

    var body: some View {
        let primary = kind == .primary
        Button(action: action) {
            Text(title)
                .font(.custom("ReemKufi-Medium", size: primary ? 16 : 14, relativeTo: .headline))
                .foregroundStyle(primary ? Color.white : Color.appPrimary)
                .padding(.horizontal, primary ? 36 : 24)
                .frame(minWidth: primary ? 200 : 0, minHeight: primary ? 52 : 42)
                .softGlass(in: Capsule(), fill: primary ? .shareCard : .softSurface, rim: !primary)
        }
        .buttonStyle(SoftPressStyle())
    }
}

struct SoftTextField: View {
    let placeholder: String
    @Binding var text: String
    var onSubmit: () -> Void = {}

    var body: some View {
        TextField("", text: $text, prompt: Text(placeholder).foregroundStyle(Color.textSecondary))
            .font(.custom("ReemKufi-Regular", size: 17, relativeTo: .body))
            .foregroundStyle(.textPrimary)
            .multilineTextAlignment(.center)
            .textInputAutocapitalization(.words)
            .submitLabel(.done)
            .onSubmit(onSubmit)
            .padding(.horizontal, 22)
            .frame(height: 52)
            .softGlass(in: Capsule())
    }
}

struct SoftPressStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
            .brightness(configuration.isPressed ? -0.02 : 0)
            .animation(.spring(response: 0.28, dampingFraction: 0.6), value: configuration.isPressed)
            .sensoryFeedback(.impact(flexibility: .soft, intensity: 0.5), trigger: configuration.isPressed) { _, pressed in pressed }
    }
}

struct SoftArtwork: View {
    enum Palette {
        case forest
        case dawn
        case lagoon
    }

    let palette: Palette
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        MeshGradient(
            width: 3,
            height: 3,
            points: [
                [0, 0], [0.55, 0], [1, 0],
                [0, 0.45], [0.4, 0.55], [1, 0.4],
                [0, 1], [0.6, 1], [1, 1],
            ],
            colors: colors
        )
    }

    private var colors: [Color] {
        let dark = colorScheme == .dark
        switch palette {
        case .forest:
            return dark
                ? [0x0B2A22, 0x114538, 0x1D5C4B, 0x14493B, 0x2F7D6B, 0x0F3A30, 0x1B5A4A, 0x3E8F84, 0x0E3A31].map { Color(hex: $0) }
                : [0x114538, 0x185E4C, 0x24735F, 0x15503F, 0x2A7C67, 0x1F6653, 0x236F5C, 0x3A8C7A, 0x4E9E8E].map { Color(hex: $0) }
        case .dawn:
            return dark
                ? [0x10251F, 0x1A3A30, 0x2A3A2A, 0x163228, 0x234A3E, 0x2E3F30, 0x12302A, 0x1E4A40, 0x283828].map { Color(hex: $0) }
                : [0xE2FFF8, 0xF6F1DC, 0xD6F2EA, 0xC9ECE3, 0xF2FBFF, 0xEFE6C4, 0xB9E4DA, 0xE4F6F0, 0xF7EFD3].map { Color(hex: $0) }
        case .lagoon:
            return dark
                ? [0x0E2A2C, 0x163C40, 0x0F2F2A, 0x1B4A4E, 0x21585A, 0x123A34, 0x0F3033, 0x1D4F52, 0x163F3A].map { Color(hex: $0) }
                : [0xCFEFF3, 0x81CACF, 0xE2FFF8, 0xA9DDE0, 0xF2FBFF, 0x9BD6CC, 0xE0F6F7, 0x7FC4C4, 0xCDEFE6].map { Color(hex: $0) }
        }
    }
}

struct SoftBackdrop: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let dark = colorScheme == .dark
        Color.softCanvas
            .overlay(alignment: .topTrailing) {
                bloom(dark ? 0x114538 : 0xD8F3EC, size: 420).offset(x: 140, y: -120)
            }
            .overlay(alignment: .bottomLeading) {
                bloom(dark ? 0x0F3A3C : 0xCFEFF3, size: 460).offset(x: -170, y: 120)
            }
            .overlay(alignment: .center) {
                bloom(dark ? 0x1A2A20 : 0xF6F1DC, size: 320).offset(x: 120, y: 160)
            }
            .ignoresSafeArea()
    }

    private func bloom(_ hex: UInt32, size: CGFloat) -> some View {
        let color = Color(hex: hex).opacity(colorScheme == .dark ? 0.55 : 0.9)
        return RadialGradient(colors: [color, color.opacity(0)], center: .center, startRadius: size * 0.1, endRadius: size * 0.78)
            .frame(width: size * 1.6, height: size * 1.6)
            .allowsHitTesting(false)
    }
}
