import Foundation
import Shared
import SwiftData

/// What's left of the Swift app's own storage: the SwiftData store from before the move to shared code, which the
/// journal and reading-progress imports read at launch
@MainActor
final class DependencyContainer {
    let modelContainer: ModelContainer

    init() {
        do {
            self.modelContainer = try DatabaseConfiguration.makeContainer()
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
        // The journal now lives in the shared database; entries written before are brought over from SwiftData once
        let modelContainer = self.modelContainer
        Task { await SwiftDataJournalImport.run(from: modelContainer) }
    }

    // MARK: - Quran

    /// Readies the shared Quran at launch, during the splash: the reading progress SwiftData kept is brought over
    /// once, and for someone past onboarding the Quran text, which the Swift code also kept in SwiftData, is
    /// downloaded into the shared database the first time after the update. The splash waits for that a little, so
    /// Home opens with its ayahs; a slow or failed download carries on or retries from the Quran tab.
    func prepareQuran() async {
        await StoredReadingProgressImport(modelContainer: modelContainer).run()
        guard OnboardingStatus.shared.isComplete() else { return }
        let sync = Task { _ = try? await QuranUseCases.shared.syncIfNeeded() }
        await withTaskGroup(of: Void.self) { group in
            group.addTask { await sync.value }
            group.addTask { try? await Task.sleep(for: .seconds(8)) }
            await group.next()
            group.cancelAll()
        }
    }
}

