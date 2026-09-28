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
        readAyahs: Set<Int>,
        totalAyahs: Int
    ) async throws {
        let descriptor = FetchDescriptor<ReadingProgressEntity>(
            predicate: #Predicate { $0.surahNumber == surahNumber }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.lastAyahNumber = lastAyahNumber
            // A union, so re-reading an ayah never counts it twice and skipping around never loses any
            let merged = Set(existing.readAyahs).union(readAyahs).sorted()
            existing.readAyahs = merged
            existing.completedAyahs = merged.count
            existing.totalAyahs = totalAyahs
            existing.lastReadAt = .now
        } else {
            let entity = ReadingProgressEntity(
                surahNumber: surahNumber,
                lastAyahNumber: lastAyahNumber,
                readAyahs: readAyahs.sorted(),
                totalAyahs: totalAyahs
            )
            modelContext.insert(entity)
        }
        try modelContext.save()
    }

    func totalAyahsRead() async throws -> Int {
        try modelContext.fetch(FetchDescriptor<ReadingProgressEntity>()).reduce(0) { $0 + $1.readAyahs.count }
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
