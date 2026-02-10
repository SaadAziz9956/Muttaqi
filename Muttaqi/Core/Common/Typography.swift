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
}

extension Font {
    static func arabic(_ size: CGFloat) -> Font {
        .custom(FontName.Arabic.regular, size: size, relativeTo: .body)
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
