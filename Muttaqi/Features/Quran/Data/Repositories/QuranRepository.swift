import Foundation
import SwiftData

@ModelActor
actor QuranRepository: QuranRepositoryProtocol {

    func getSurahs() async throws -> [Surah] {
        let descriptor = FetchDescriptor<SurahEntity>(
            sortBy: [SortDescriptor(\.number, order: .forward)]
        )
        let entities = try modelContext.fetch(descriptor)
        return entities.map { $0.toDomain() }
    }

    func getSurah(number: Int) async throws -> Surah? {
        let descriptor = FetchDescriptor<SurahEntity>(
            predicate: #Predicate { $0.number == number }
        )
        return try modelContext.fetch(descriptor).first?.toDomain()
    }

    func getAyahs(surahNumber: Int, language: String) async throws -> [Ayah] {
        let descriptor = FetchDescriptor<AyahEntity>(
            predicate: #Predicate { $0.surahNumber == surahNumber },
            sortBy: [SortDescriptor(\.numberInSurah, order: .forward)]
        )
        let entities = try modelContext.fetch(descriptor)
        return entities.map { entity in
            let translation = entity.translations
                .first { $0.language == language }?.text
            return entity.toDomain(translation: translation)
        }
    }

    func isDataAvailable() async -> Bool {
        let descriptor = FetchDescriptor<SurahEntity>()
        let count = (try? modelContext.fetchCount(descriptor)) ?? 0
        return count == 114
    }
}

// MARK: - Entity → Domain Mapping
private extension SurahEntity {
    func toDomain() -> Surah {
        Surah(
            id: number,
            number: number,
            name: name,
            englishName: englishName,
            englishNameTranslation: englishNameTranslation,
            revelationType: revelationType,
            numberOfAyahs: numberOfAyahs
        )
    }
}

private extension AyahEntity {
    func toDomain(translation: String?) -> Ayah {
        Ayah(
            id: number,
            number: number,
            numberInSurah: numberInSurah,
            surahNumber: surahNumber,
            arabicText: arabicText,
            transliteration: transliteration,
            translation: translation,
            juz: juz,
            page: page,
            hizbQuarter: hizbQuarter
        )
    }
}
