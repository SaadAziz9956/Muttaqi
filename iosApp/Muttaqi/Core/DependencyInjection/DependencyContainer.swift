import Foundation
import Shared
import SwiftData

@MainActor
final class DependencyContainer {
    let modelContainer: ModelContainer

    init() {
        do {
            self.modelContainer = try DatabaseConfiguration.makeContainer()
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
        let modelContainer = self.modelContainer
        Task { await SwiftDataJournalImport.run(from: modelContainer) }
    }

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

    func prepareContent() async {
        _ = try? await ContentPreparation.shared.prepare()
    }
}

