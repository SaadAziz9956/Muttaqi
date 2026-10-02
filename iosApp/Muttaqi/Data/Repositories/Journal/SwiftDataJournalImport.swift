import Foundation
import Shared
import SwiftData

@MainActor
enum SwiftDataJournalImport {
    static func run(from container: ModelContainer) async {
        let importer = JournalSwiftDataImport.shared
        guard importer.isNeeded else { return }
        do {
            let stored = try container.mainContext.fetch(FetchDescriptor<JournalEntryEntity>())
            let entries = stored.map {
                importer.entry(id: $0.id.uuidString, title: $0.title, body: $0.body, createdAt: $0.createdAt, updatedAt: $0.updatedAt)
            }
            try await importer.importEntries(entries: entries)
        } catch {
        }
    }
}
