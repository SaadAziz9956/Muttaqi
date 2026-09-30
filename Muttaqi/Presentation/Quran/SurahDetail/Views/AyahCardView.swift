import SwiftUI

struct AyahCardView: View {
    let ayah: Ayah
    let fontSize: FontSize
    let language: Language
    /// Opens the explanation at this ayah
    let onExplanation: () -> Void
    @Environment(AppRouter.self) private var router
    @State private var didCopy = false

    private var isUrdu: Bool { language == .urdu }
    private var isHindi: Bool { language == .hindi }

    private var cleanArabicText: String {
        ayah.arabicText
            .replacingOccurrences(of: "\u{06DD}", with: "")
            .trimmingCharacters(in: .whitespaces)
            .kfgqpcEncoded
    }

    private var attributedAyah: AttributedString {
        var text = AttributedString(cleanArabicText + " ")
        text.font = .arabic(fontSize.arabicSize)
        text.foregroundColor = .textPrimary

        var openParen = AttributedString("\u{FD3F}")
        openParen.font = .arabic(10)
        openParen.foregroundColor = .appPrimary

        var num = AttributedString("\(ayah.numberInSurah.arabicNumeral)")
        num.font = .arabic(14)
        num.foregroundColor = .appPrimary

        var closeParen = AttributedString("\u{FD3E}")
        closeParen.font = .arabic(10)
        closeParen.foregroundColor = .appPrimary

        text.append(openParen)
        text.append(num)
        text.append(closeParen)
        return text
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(attributedAyah)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 6)

            if let transliteration = ayah.transliteration, !transliteration.isEmpty {
                Text(transliteration)
                    .font(.custom("ReemKufi-Regular", size: fontSize.transliterationSize))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 12)
            }

            if let translation = ayah.translation, !translation.isEmpty {
                if isUrdu {
                    Text(translation)
                        .font(.urduNastaliq(fontSize.translationSize))
                        .foregroundStyle(.textPrimary)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .padding(.top, 8)
                } else if isHindi {
                    Text("\(ayah.numberInSurah).  \(translation)")
                        .font(.hindiDevanagari(fontSize.translationSize))
                        .foregroundStyle(.textPrimary)
                        .padding(.top, 8)
                } else {
                    Text("\(ayah.numberInSurah).  \(translation)")
                        .font(.custom("ReemKufi-Regular", size: fontSize.translationSize))
                        .foregroundStyle(.textPrimary)
                        .padding(.top, 8)
                }
            }

            actions
                .padding(.top, 16)
        }
        .padding(18)
        .softCard(cornerRadius: 26)
        .padding(.top, 14)
        .contextMenu {
            Button("Copy", systemImage: "doc.on.doc", action: copy)
            Button("Explanation", systemImage: "book", action: onExplanation)
            Button("Share", systemImage: "square.and.arrow.up") { router.push(SharePassage(ayah: ayah)) }
        }
        .sensoryFeedback(.success, trigger: didCopy) { _, copied in copied }
    }

    private var reference: String {
        "\(ayah.surahNumber):\(ayah.numberInSurah)"
    }

    private var actions: some View {
        // Native glass rendered as one group, which is cheaper than each on its own
        GlassEffectContainer(spacing: 4) {
            HStack(spacing: 10) {
                Text(reference)
                    .font(.custom("ReemKufi-Regular", size: 12))
                    .foregroundStyle(.brandTeal)
                    .padding(.horizontal, 10)
                    .frame(height: 26)
                    .background(.tintedSurface, in: .capsule)
                    .accessibilityLabel("Ayah \(reference)")

                Spacer(minLength: 0)

                actionButton("book-linear", label: "Explanation", action: onExplanation)
                actionButton(didCopy ? "tick-circle-linear" : "copy-linear", label: didCopy ? "Copied" : "Copy", action: copy)
                NavigationLink(value: SharePassage(ayah: ayah)) {
                    actionFace("export-arrow-01-linear")
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityLabel("Share")
            }
        }
    }

    private func actionButton(_ icon: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) { actionFace(icon) }
            .buttonStyle(SoftPressStyle())
            .accessibilityLabel(label)
    }

    private func actionFace(_ icon: String) -> some View {
        SoftCircle(size: 34) {
            Image(icon)
                .resizable()
                .frame(width: 16, height: 16)
        }
    }

    private func copy() {
        UIPasteboard.general.string = [ayah.arabicText, ayah.translation, "Quran (\(reference))"]
            .compactMap { $0 }
            .joined(separator: "\n\n")
        didCopy = true
        Task {
            try? await Task.sleep(for: .seconds(1.5))
            didCopy = false
        }
    }
}

private extension Int {
    var arabicNumeral: String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "ar")
        return formatter.string(from: NSNumber(value: self)) ?? "\(self)"
    }
}
