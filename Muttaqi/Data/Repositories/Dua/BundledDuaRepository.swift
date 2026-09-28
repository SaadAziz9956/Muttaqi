import Foundation

/// Quranic duas bundled in Duas.json. Each entry is the dua portion of an ayah, cut from the same Quran
/// editions the app downloads (Uthmani Arabic, transliteration, Sahih International, Jalandhry).
final class BundledDuaRepository: DuaRepositoryProtocol {
    private struct Entry: Decodable {
        let surah: Int
        let ayah: Int
        let arabic: String
        let transliteration: String
        let translations: [String: String]
    }

    private let bundle: Bundle
    private var entries: [Entry]?

    init(bundle: Bundle = .main) {
        self.bundle = bundle
    }

    func duas(language: String) throws -> [Dua] {
        try loadEntries().map { entry in
            Dua(
                surahNumber: entry.surah,
                ayahNumber: entry.ayah,
                arabic: entry.arabic,
                transliteration: entry.transliteration,
                // Hindi isn't included: that edition's verse numbering drifts in places, so it falls back to English
                translation: entry.translations[language] ?? entry.translations["en"] ?? ""
            )
        }
    }

    private func loadEntries() throws -> [Entry] {
        if let entries { return entries }
        guard let url = bundle.url(forResource: "Duas", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        let loaded = try JSONDecoder().decode([Entry].self, from: Data(contentsOf: url))
        entries = loaded
        return loaded
    }
}
