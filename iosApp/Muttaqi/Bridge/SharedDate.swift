import Foundation
import Shared

extension Date {
    init(_ instant: KotlinInstant) {
        self.init(timeIntervalSince1970: TimeInterval(instant.toEpochMilliseconds()) / 1000)
    }
}
