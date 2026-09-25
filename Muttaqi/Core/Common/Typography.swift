import SwiftUI

private enum FontName {
    enum ReemKufi {
        static let regular = "ReemKufi-Regular"
        static let medium = "ReemKufi-Medium"
        static let semiBold = "ReemKufi-SemiBold"
        static let bold = "ReemKufi-Bold"
    }

    enum Arabic {
        static let regular = "kfgqpchafsuthmanicscript-Reg"
    }

    // iOS system Nastaliq font — available on all iOS 9+ devices, no bundling needed
    enum Urdu {
        static let nastaliq = "NotoNastaliqUrdu"
    }

    // Apple's own Devanagari font — available on all iOS 9+ devices, no bundling needed
    enum Hindi {
        static let devanagari = "KohinoorDevanagari-Regular"
    }
}

extension String {
    /// Re-encodes standard Uthmani text for the KFGQPC Hafs font used by `Font.arabic`, which assigns some marks
    /// differently: its sukun is U+06E1 and its silent-letter circle is U+0652, so the standard U+06DF would draw
    /// as a dotted-circle placeholder. Mapping checked against quran.com's QPC Hafs text for all 6,236 ayahs.
    nonisolated var kfgqpcEncoded: String {
        String(String.UnicodeScalarView(unicodeScalars.map { scalar -> Unicode.Scalar in
            switch scalar.value {
            case 0x0652: "\u{06E1}" // sukun
            case 0x06DF: "\u{0652}" // silent-letter circle
            case 0x06E3: "\u{06DC}" // small seen (52:37)
            case 0x06EB: "\u{06EC}" // ishmam (12:11)
            default: scalar
            }
        }))
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
    
    static let displayLarge = Font.custom(FontName.ReemKufi.bold, size: 34, relativeTo: .largeTitle)
    static let displayMedium = Font.custom(FontName.ReemKufi.bold, size: 28, relativeTo: .title)
    
    static let titleLarge = Font.custom(FontName.ReemKufi.semiBold, size: 22, relativeTo: .title2)
    static let titleMedium = Font.custom(FontName.ReemKufi.semiBold, size: 18, relativeTo: .title3)
    static let titleSmall = Font.custom(FontName.ReemKufi.medium, size: 16, relativeTo: .headline)
    
    static let bodyLarge = Font.custom(FontName.ReemKufi.regular, size: 17, relativeTo: .body)
    static let bodyMedium = Font.custom(FontName.ReemKufi.regular, size: 15, relativeTo: .subheadline)
    static let bodySmall = Font.custom(FontName.ReemKufi.regular, size: 13, relativeTo: .footnote)
    
    static let labelLarge = Font.custom(FontName.ReemKufi.medium, size: 14, relativeTo: .footnote)
    static let labelMedium = Font.custom(FontName.ReemKufi.medium, size: 12, relativeTo: .caption)
    static let labelSmall = Font.custom(FontName.ReemKufi.regular, size: 12, relativeTo: .caption2)
}
