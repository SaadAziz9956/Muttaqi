import Foundation

extension String {
    /// For matching search text: case- and accent-insensitive. Foundation's diacritic folding leaves Arabic harakat in
    /// place, so those are stripped here, along with tatweel, and letter variants are unified, so Arabic matches with
    /// or without vowels
    var searchFolded: String {
        let folded = folding(options: [.caseInsensitive, .diacriticInsensitive, .widthInsensitive], locale: nil)
        let scalars = folded.unicodeScalars.compactMap { scalar -> Unicode.Scalar? in
            switch scalar.value {
            case 0x064B...0x065F, 0x0670, 0x06D6...0x06ED, 0x0640: return nil
            case 0x0622, 0x0623, 0x0625, 0x0671: return "\u{0627}" // آ أ إ ٱ → ا
            case 0x0629: return "\u{0647}"                          // ة → ه
            case 0x0649: return "\u{064A}"                          // ى → ي
            default: return scalar
            }
        }
        return String(String.UnicodeScalarView(scalars))
    }
}
