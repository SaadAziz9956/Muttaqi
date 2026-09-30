import Foundation
import Shared
import SwiftData

/// Hands the reading progress the Swift code kept in SwiftData to the shared Quran database, once, so no one loses
/// their place in the move to shared code. SwiftData can only be read from Swift, so Swift reads the rows and the
/// shared import combines them with anything recorded since and remembers it's done.
@MainActor
struct StoredReadingProgressImport {
    let modelContainer: ModelContainer

    func run() async {
        let quran = QuranUseCases.shared
        guard quran.isStoredReadingProgressImportNeeded else { return }
        // A store that can't be read is left for the next launch rather than marked as imported
        guard let rows = try? modelContainer.mainContext.fetch(FetchDescriptor<ReadingProgressEntity>()) else { return }
        let records = rows.map { row in
            StoredReadingProgress(
                surahNumber: Int32(row.surahNumber),
                lastAyahNumber: Int32(row.lastAyahNumber),
                readAyahs: row.readAyahs.map { KotlinInt(value: Int32($0)) },
                completedAyahs: Int32(row.completedAyahs),
                totalAyahs: Int32(row.totalAyahs),
                lastReadAtEpochMillis: Int64((row.lastReadAt.timeIntervalSince1970 * 1000).rounded())
            )
        }
        _ = try? await quran.importStoredReadingProgress(records: records)
    }
}
