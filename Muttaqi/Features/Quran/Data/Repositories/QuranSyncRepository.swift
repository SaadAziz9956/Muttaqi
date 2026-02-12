import Foundation
import SwiftData

actor QuranSyncRepository: QuranSyncRepositoryProtocol, ModelActor {
    nonisolated let modelExecutor: any SwiftData.ModelExecutor
    nonisolated let modelContainer: SwiftData.ModelContainer
    private let apiService: QuranAPIServiceProtocol

    init(modelContainer: ModelContainer, apiService: QuranAPIServiceProtocol) {
        let context = ModelContext(modelContainer)
        context.autosaveEnabled = false
        self.modelExecutor = DefaultSerialModelExecutor(modelContext: context)
        self.modelContainer = modelContainer
        self.apiService = apiService
    }

    func syncSurahList() async throws {
        let dtos = try await apiService.fetchSurahList()
        for dto in dtos {
            let entity = SurahEntity(
                number: dto.number,
                name: dto.name,
                englishName: dto.englishName,
                englishNameTranslation: dto.englishNameTranslation,
                revelationType: dto.revelationType,
                numberOfAyahs: dto.numberOfAyahs
            )
            modelContext.insert(entity)
        }
        try modelContext.save()
    }

    func syncArabicAndTransliteration() async throws {
        // 1. Fetch Arabic text
        let arabicData = try await apiService.fetchFullQuran(edition: QuranEdition.arabicUthmani)

        // 2. Fetch transliteration
        let translitData = try await apiService.fetchFullQuran(edition: QuranEdition.englishTransliteration)

        // Build transliteration lookup: global ayah number → text
        var translitLookup: [Int: String] = [:]
        for surah in translitData.surahs {
            for ayah in surah.ayahs {
                translitLookup[ayah.number] = ayah.text
            }
        }

        // 3. Fetch surah list first to get/create surah entities
        let surahDescriptor = FetchDescriptor<SurahEntity>(
            sortBy: [SortDescriptor(\.number)]
        )
        var existingSurahs = try modelContext.fetch(surahDescriptor)

        // If surahs don't exist yet, create them from arabicData
        if existingSurahs.isEmpty {
            for surahDTO in arabicData.surahs {
                let entity = SurahEntity(
                    number: surahDTO.number,
                    name: surahDTO.name,
                    englishName: surahDTO.englishName,
                    englishNameTranslation: surahDTO.englishNameTranslation,
                    revelationType: surahDTO.revelationType,
                    numberOfAyahs: surahDTO.numberOfAyahs
                )
                modelContext.insert(entity)
            }
            try modelContext.save()
            existingSurahs = try modelContext.fetch(surahDescriptor)
        }

        // Build surah lookup
        let surahLookup = Dictionary(uniqueKeysWithValues: existingSurahs.map { ($0.number, $0) })

        // 4. Insert ayahs with Arabic + transliteration
        for surahDTO in arabicData.surahs {
            guard let surahEntity = surahLookup[surahDTO.number] else { continue }

            for ayahDTO in surahDTO.ayahs {
                let ayahEntity = AyahEntity(
                    number: ayahDTO.number,
                    numberInSurah: ayahDTO.numberInSurah,
                    surahNumber: surahDTO.number,
                    arabicText: ayahDTO.text,
                    transliteration: translitLookup[ayahDTO.number],
                    juz: ayahDTO.juz,
                    page: ayahDTO.page,
                    hizbQuarter: ayahDTO.hizbQuarter
                )
                ayahEntity.surah = surahEntity
                modelContext.insert(ayahEntity)
            }
        }

        try modelContext.save()
    }

    func syncTranslation(language: String) async throws {
        let edition = QuranEdition.translationEdition(for: language)
        let translationData = try await apiService.fetchFullQuran(edition: edition)

        // Fetch existing ayahs to link translations
        let ayahDescriptor = FetchDescriptor<AyahEntity>(
            sortBy: [SortDescriptor(\.number)]
        )
        let existingAyahs = try modelContext.fetch(ayahDescriptor)
        let ayahLookup = Dictionary(uniqueKeysWithValues: existingAyahs.map { ($0.number, $0) })

        // Delete existing translations for this language before inserting new ones
        let existingTranslations = FetchDescriptor<AyahTranslationEntity>(
            predicate: #Predicate { $0.language == language }
        )
        let toDelete = try modelContext.fetch(existingTranslations)
        for entity in toDelete {
            modelContext.delete(entity)
        }

        // Insert translations
        for surah in translationData.surahs {
            for ayahDTO in surah.ayahs {
                guard let ayahEntity = ayahLookup[ayahDTO.number] else { continue }

                let translationEntity = AyahTranslationEntity(
                    ayahNumber: ayahDTO.number,
                    language: language,
                    editionIdentifier: edition,
                    text: ayahDTO.text
                )
                translationEntity.ayah = ayahEntity
                modelContext.insert(translationEntity)
            }
        }

        try modelContext.save()
    }
}
