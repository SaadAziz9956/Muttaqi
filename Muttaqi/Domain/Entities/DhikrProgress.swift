import Foundation

/// How far the reader has got with one dhikr today
struct DhikrProgress: Codable, Equatable, Sendable {
    /// Repetitions in the current round
    var count: Int
    /// Rounds finished today; only counted for a dhikr with a target
    var rounds: Int
    /// The day this progress belongs to; any other day starts again from zero
    var day: Date

    static func empty(on day: Date = .now) -> DhikrProgress {
        DhikrProgress(count: 0, rounds: 0, day: Calendar.current.startOfDay(for: day))
    }
}
