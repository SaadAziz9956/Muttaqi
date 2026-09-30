import Foundation
import Shared

extension Date {
    /// A moment from the shared (Kotlin) code, e.g. a prayer time, as a Foundation date to format and compare
    init(_ instant: KotlinInstant) {
        self.init(timeIntervalSince1970: TimeInterval(instant.toEpochMilliseconds()) / 1000)
    }
}
