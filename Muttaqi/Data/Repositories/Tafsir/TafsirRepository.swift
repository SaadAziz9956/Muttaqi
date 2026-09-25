import Foundation
import SwiftData

actor TafsirRepository: TafsirRepositoryProtocol, ModelActor {
    nonisolated let modelExecutor: any SwiftData.ModelExecutor
    nonisolated let modelContainer: SwiftData.ModelContainer
    private let apiService: TafsirAPIServiceProtocol

    init(modelContainer: ModelContainer, apiService: TafsirAPIServiceProtocol) {
        let context = ModelContext(modelContainer)
        context.autosaveEnabled = false
        self.modelExecutor = DefaultSerialModelExecutor(modelContext: context)
        self.modelContainer = modelContainer
        self.apiService = apiService
    }

    func getTafsir(surahNumber: Int, languageCode: String) async throws -> [TafsirAyah] {
        let cached = try fetchCached(surahNumber: surahNumber, languageCode: languageCode)
        if !cached.isEmpty { return cached }

        let source = Self.source(for: languageCode)
        let response = try await apiService.fetchTafsir(tafsirId: source.id, surahNumber: surahNumber)

        for dto in response.tafsirs {
            // When commentary covers several ayahs, the API puts it on the first one and leaves the rest empty
            let text = dto.text.strippingHTML()
            guard !text.isEmpty else { continue }

            let entity = TafsirEntity(
                surahNumber: surahNumber,
                ayahNumber: parseAyahNumber(from: dto.verseKey),
                language: languageCode,
                tafsirSource: source.name,
                text: text
            )
            modelContext.insert(entity)
        }
        try modelContext.save()

        return try fetchCached(surahNumber: surahNumber, languageCode: languageCode)
    }

    private func fetchCached(surahNumber: Int, languageCode: String) throws -> [TafsirAyah] {
        let descriptor = FetchDescriptor<TafsirEntity>(
            predicate: #Predicate { $0.surahNumber == surahNumber && $0.language == languageCode },
            sortBy: [SortDescriptor(\.ayahNumber)]
        )
        let entities = try modelContext.fetch(descriptor)
        guard let last = entities.last else { return [] }

        // Each entry covers every ayah up to where the next entry starts; the final one runs to the end of the surah
        let surahEnd = try ayahCount(of: surahNumber) ?? last.ayahNumber
        return entities.enumerated().map { index, entity in
            let nextStart = index + 1 < entities.count ? entities[index + 1].ayahNumber : surahEnd + 1
            return TafsirAyah(
                id: entity.ayahNumber,
                ayahNumber: entity.ayahNumber,
                lastAyahNumber: max(entity.ayahNumber, nextStart - 1),
                verseKey: "\(surahNumber):\(entity.ayahNumber)",
                text: entity.text
            )
        }
    }

    private func ayahCount(of surahNumber: Int) throws -> Int? {
        var descriptor = FetchDescriptor<SurahEntity>(
            predicate: #Predicate { $0.number == surahNumber }
        )
        descriptor.fetchLimit = 1
        return try modelContext.fetch(descriptor).first?.numberOfAyahs
    }

    private func parseAyahNumber(from verseKey: String) -> Int {
        Int(verseKey.split(separator: ":").last ?? "") ?? 0
    }

    private nonisolated static func source(for languageCode: String) -> (id: Int, name: String) {
        switch languageCode {
        case "ur": return (160, "Tafsir Ibn Kathir")
        // quran.com has no Hindi tafsir, so Hindi readers get the English one
        default: return (169, "Ibn Kathir (Abridged)")
        }
    }
}

private extension String {
    nonisolated func strippingHTML() -> String {
        self
            // Turn block boundaries into blank lines before dropping tags, so paragraphs and headings stay separate
            .replacingOccurrences(of: "<br\\s*/?>", with: "\n", options: [.regularExpression, .caseInsensitive])
            .replacingOccurrences(of: "</?(p|div|h[1-6])(\\s[^>]*)?>", with: "\n\n", options: [.regularExpression, .caseInsensitive])
            .replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression)
            .replacingOccurrences(of: "&amp;", with: "&")
            .replacingOccurrences(of: "&lt;", with: "<")
            .replacingOccurrences(of: "&gt;", with: ">")
            .replacingOccurrences(of: "&quot;", with: "\"")
            .replacingOccurrences(of: "&#39;", with: "'")
            .replacingOccurrences(of: "&nbsp;", with: " ")
            .replacingOccurrences(of: "[ \\t]*\\n(\\s*\\n)+[ \\t]*", with: "\n\n", options: .regularExpression)
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }
}
