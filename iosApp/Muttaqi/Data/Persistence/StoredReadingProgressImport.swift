import Foundation
import Shared
import SwiftData

@MainActor
struct StoredReadingProgressImport {
    let modelContainer: ModelContainer

    func run() async {
        let quran = QuranUseCases.shared
        guard quran.isStoredReadingProgressImportNeeded else { return }
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
