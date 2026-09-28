import Foundation

protocol DhikrProgressStoring: Sendable {
    /// Today's progress for the dhikr, or zero if it hasn't been said yet today
    func progress(for dhikrID: String) -> DhikrProgress
    func save(_ progress: DhikrProgress, for dhikrID: String)
}

/// Keeps each dhikr's count for the day, so leaving the counter and coming back carries on where it stopped
final class DhikrProgressStore: DhikrProgressStoring, @unchecked Sendable {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func progress(for dhikrID: String) -> DhikrProgress {
        guard let data = defaults.data(forKey: Self.key(dhikrID)),
              let saved = try? JSONDecoder().decode(DhikrProgress.self, from: data),
              Calendar.current.isDateInToday(saved.day)
        else { return .empty() }
        return saved
    }

    func save(_ progress: DhikrProgress, for dhikrID: String) {
        defaults.set(try? JSONEncoder().encode(progress), forKey: Self.key(dhikrID))
    }

    private static func key(_ dhikrID: String) -> String {
        "dhikr_progress.\(dhikrID)"
    }
}
