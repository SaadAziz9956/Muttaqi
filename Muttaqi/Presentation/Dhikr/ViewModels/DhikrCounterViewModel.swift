import Foundation

/// Counts repetitions of one dhikr. The count is kept for the day, so it carries on after leaving the screen.
@Observable
@MainActor
final class DhikrCounterViewModel {
    /// What a repetition completed, so the view can give a stronger haptic at the end of a phrase or a round
    enum Milestone {
        case repetition, phraseFinished, roundFinished
    }

    let dhikr: Dhikr
    private(set) var progress: DhikrProgress

    private let store: DhikrProgressStoring

    init(dhikr: Dhikr, store: DhikrProgressStoring) {
        self.dhikr = dhikr
        self.store = store
        self.progress = store.progress(for: dhikr.id)
    }

    var count: Int { progress.count }
    var rounds: Int { progress.rounds }
    var target: Int? { dhikr.target }

    var isRoundComplete: Bool {
        target.map { count >= $0 } ?? false
    }

    /// Share of the current round said, from 0 to 1; always 0 for open-ended remembrance
    var roundProgress: Double {
        guard let target, target > 0 else { return 0 }
        return min(Double(count) / Double(target), 1)
    }

    /// For a set said in parts: the phrase to say now, and how many of it have been said
    var currentStep: (index: Int, said: Int)? {
        guard let last = dhikr.steps.indices.last else { return nil }
        var start = 0
        for (index, step) in dhikr.steps.enumerated() {
            if count < start + step.count { return (index, count - start) }
            start += step.count
        }
        return (last, dhikr.steps[last].count)
    }

    var hasProgress: Bool { count > 0 || rounds > 0 }

    func increment() {
        // A count left from an earlier day, e.g. with the screen open past midnight, starts again
        if !Calendar.current.isDateInToday(progress.day) { progress = .empty() }
        // The tap after a finished round starts the next one
        if isRoundComplete { progress.count = 0 }
        progress.count += 1
        if isRoundComplete { progress.rounds += 1 }
        store.save(progress, for: dhikr.id)
    }

    func reset() {
        progress = .empty()
        store.save(progress, for: dhikr.id)
    }

    func milestone(at count: Int) -> Milestone {
        if let target, count == target { return .roundFinished }
        var phraseEnd = 0
        for step in dhikr.steps.dropLast() {
            phraseEnd += step.count
            if count == phraseEnd { return .phraseFinished }
        }
        return .repetition
    }
}
