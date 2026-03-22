import Foundation
import SwiftData

@ModelActor
actor ReadingProgressRepository: ReadingProgressRepositoryProtocol {

    func getProgress(surahNumber: Int) async throws -> ReadingProgressData? {
        let descriptor = FetchDescriptor<ReadingProgressEntity>(
            predicate: #Predicate { $0.surahNumber == surahNumber }
        )
        return try modelContext.fetch(descriptor).first?.toData()
    }

    func getLastRead() async throws -> ReadingProgressData? {
        var descriptor = FetchDescriptor<ReadingProgressEntity>(
            sortBy: [SortDescriptor(\.lastReadAt, order: .reverse)]
        )
        descriptor.fetchLimit = 1
        return try modelContext.fetch(descriptor).first?.toData()
    }

    func updateProgress(
        surahNumber: Int,
        lastAyahNumber: Int,
        completedAyahs: Int,
        totalAyahs: Int
    ) async throws {
        let descriptor = FetchDescriptor<ReadingProgressEntity>(
            predicate: #Predicate { $0.surahNumber == surahNumber }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.lastAyahNumber = lastAyahNumber
            existing.completedAyahs = completedAyahs
            existing.totalAyahs = totalAyahs
            existing.lastReadAt = .now
        } else {
            let entity = ReadingProgressEntity(
                surahNumber: surahNumber,
                lastAyahNumber: lastAyahNumber,
                completedAyahs: completedAyahs,
                totalAyahs: totalAyahs
            )
            modelContext.insert(entity)
        }
        try modelContext.save()
    }
}

private extension ReadingProgressEntity {
    func toData() -> ReadingProgressData {
        ReadingProgressData(
            surahNumber: surahNumber,
            lastAyahNumber: lastAyahNumber,
            completedAyahs: completedAyahs,
            totalAyahs: totalAyahs,
            lastReadAt: lastReadAt
        )
    }
}
