import SwiftUI

private enum FontName {
    enum ReemKufi {
        static let regular = "ReemKufi-Regular"
        static let medium = "ReemKufi-Medium"
        static let semiBold = "ReemKufi-SemiBold"
    }

    enum Arabic {
        static let regular = "kfgqpchafsuthmanicscript-Reg"
    }

    enum Urdu {
        static let nastaliq = "NotoNastaliqUrdu"
    }

    enum Hindi {
        static let devanagari = "KohinoorDevanagari-Regular"
    }
}

extension String {
    nonisolated var kfgqpcEncoded: String {
        String(String.UnicodeScalarView(unicodeScalars.map { scalar -> Unicode.Scalar in
            switch scalar.value {
            case 0x0652: "\u{06E1}"
            case 0x06DF: "\u{0652}"
            case 0x06E3: "\u{06DC}"
            case 0x06EB: "\u{06EC}"
            default: scalar
            }
        }))
    }
}

extension AttributedString {
    static func arabic(_ text: String, size: CGFloat) -> AttributedString {
        var styled = AttributedString(text.kfgqpcEncoded)
        styled.font = .arabic(size)
        for mark in ["،", "؛", "؟", "﴿", "﴾"] {
            var searchStart = styled.startIndex
            while let range = styled[searchStart...].range(of: mark) {
                styled[range].font = .system(size: size * 0.9)
                searchStart = range.upperBound
            }
        }
        return styled
    }
}

extension Font {
    static func arabic(_ size: CGFloat) -> Font {
        .custom(FontName.Arabic.regular, size: size, relativeTo: .body)
    }

    static func urduNastaliq(_ size: CGFloat) -> Font {
        .custom(FontName.Urdu.nastaliq, size: size, relativeTo: .body)
    }

    static func hindiDevanagari(_ size: CGFloat) -> Font {
        .custom(FontName.Hindi.devanagari, size: size, relativeTo: .body)
    }
    
    static let titleMedium = Font.custom(FontName.ReemKufi.semiBold, size: 18, relativeTo: .title3)
    static let titleSmall = Font.custom(FontName.ReemKufi.medium, size: 16, relativeTo: .headline)
    
    static let bodyLarge = Font.custom(FontName.ReemKufi.regular, size: 17, relativeTo: .body)
    static let bodyMedium = Font.custom(FontName.ReemKufi.regular, size: 15, relativeTo: .subheadline)
    static let bodySmall = Font.custom(FontName.ReemKufi.regular, size: 13, relativeTo: .footnote)
    
    static let labelLarge = Font.custom(FontName.ReemKufi.medium, size: 14, relativeTo: .footnote)
    static let labelMedium = Font.custom(FontName.ReemKufi.medium, size: 12, relativeTo: .caption)
    static let labelSmall = Font.custom(FontName.ReemKufi.regular, size: 12, relativeTo: .caption2)
}
