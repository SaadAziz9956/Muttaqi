import Shared
import SwiftUI
import UIKit

struct ArabicOnlyView: View {
    let pages: [MushafPage]
    let fontSize: FontSize

    var body: some View {
        VStack(spacing: 0) {
            ForEach(pages, id: \.id) { page in
                VStack(spacing: 20) {
                    MushafPageText(ayahs: page.ayahs, fontSize: CGFloat(fontSize.arabicSize))

                    HStack(spacing: 12) {
                        hairline
                        Text("\(page.number)")
                            .font(.labelSmall)
                            .foregroundStyle(.textSecondary)
                        hairline
                    }
                }
                .padding(18)
                .softCard(cornerRadius: 26)
                .padding(.vertical, 8)
            }
        }
        .scrollTargetLayout()
    }

    private var hairline: some View {
        Rectangle()
            .fill(Color(.systemGray5))
            .frame(height: 1)
    }
}

private struct MushafPageText: UIViewRepresentable {
    let ayahs: [Ayah]
    let fontSize: CGFloat

    func makeUIView(context: Context) -> UITextView {
        let textView = UITextView()
        textView.isEditable = false
        textView.isSelectable = false
        textView.isScrollEnabled = false
        textView.backgroundColor = .clear
        textView.textContainerInset = .zero
        textView.textContainer.lineFragmentPadding = 0
        textView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return textView
    }

    func updateUIView(_ textView: UITextView, context: Context) {
        textView.attributedText = pageText
    }

    func sizeThatFits(_ proposal: ProposedViewSize, uiView textView: UITextView, context: Context) -> CGSize? {
        guard let width = proposal.width, width.isFinite else { return nil }
        let height = textView.sizeThatFits(CGSize(width: width, height: .greatestFiniteMagnitude)).height
        return CGSize(width: width, height: ceil(height))
    }

    private var pageText: NSAttributedString {
        let font = UIFontMetrics(forTextStyle: .body).scaledFont(
            for: UIFont(name: "kfgqpchafsuthmanicscript-Reg", size: fontSize) ?? .systemFont(ofSize: fontSize)
        )
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .justified
        paragraph.baseWritingDirection = .rightToLeft
        paragraph.lineSpacing = fontSize * 0.6

        let text = NSMutableAttributedString()
        for (index, ayah) in ayahs.enumerated() {
            let words = ayah.arabicWithoutEndSign().kfgqpcEncoded
            text.append(NSAttributedString(string: words + " ", attributes: [
                .font: font, .paragraphStyle: paragraph, .foregroundColor: UIColor(resource: .textPrimary),
            ]))
            let separator = index == ayahs.count - 1 ? "" : " "
            text.append(NSAttributedString(string: Self.arabicIndicDigits(Int(ayah.numberInSurah)) + separator, attributes: [
                .font: font, .paragraphStyle: paragraph, .foregroundColor: UIColor(resource: .appPrimary),
            ]))
        }
        return text
    }

    private static func arabicIndicDigits(_ number: Int) -> String {
        String(String(number).compactMap { digit in
            digit.wholeNumberValue.flatMap { Character(UnicodeScalar(0x0660 + $0)!) }
        })
    }
}
