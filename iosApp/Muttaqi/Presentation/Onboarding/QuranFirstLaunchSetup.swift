import Foundation
import Shared

/// The download the first launch waits for, for the shared onboarding: the Quran's Arabic, transliteration and
/// English translation
final class QuranFirstLaunchSetup: NSObject, FirstLaunchSetup {
    private let quran: QuranUseCases

    init(quran: QuranUseCases) {
        self.quran = quran
    }

    nonisolated func run(onDone: @escaping () -> Void, onFailed: @escaping (String) -> Void) {
        Task { @MainActor in
            let outcome = try? await quran.sync(language: .english)
            if let outcome, !(outcome is OutcomeFailure) {
                onDone()
            } else {
                onFailed(QuranMessages.shared.downloadFailed(language: .english))
            }
        }
    }
}
