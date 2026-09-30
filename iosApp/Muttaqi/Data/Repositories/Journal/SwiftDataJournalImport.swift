import Foundation
import Shared
import SwiftData

/// Brings the journal the app kept in SwiftData, before the move to shared code, into the shared database, once, with
/// each entry's own id and dates. It's marked done (in the standard user defaults) only once every entry is in, so an
/// interrupted import runs again at the next launch. The SwiftData rows are left as they were.
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
            // Tried again at the next launch
        }
    }
}
