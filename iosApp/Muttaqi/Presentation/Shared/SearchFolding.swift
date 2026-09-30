import Foundation
import Shared

extension String {
    /// For matching search text: case- and accent-insensitive, and Arabic matches with or without its vowels. The
    /// folding itself is shared with Android, in the Kotlin module's `SearchTextFolder`
    var searchFolded: String {
        SearchTextFolder.shared.fold(text: self)
    }
}
