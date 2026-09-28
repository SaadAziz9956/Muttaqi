import Foundation

/// Duas bundled with the app:
/// - Duas.json: Quranic duas, each the dua portion of an ayah, cut from the same Quran editions the app downloads
///   (Uthmani Arabic, transliteration, Sahih International, Jalandhry)
/// - HisnAlMuslim.json: Hisn al-Muslim by Sa'id al-Qahtani, grouped into categories. Text, transliteration and
///   English from hisnmuslim.com; Urdu, where published, from Hafiz Zubair Ali Za'i's Mukhtasar Hisn al-Muslim
///   (islamicurdubooks.com) or HadeethEnc; hadith references from the book's footnotes (github.com/rn0x/hisn_almuslim_json)
final class BundledDuaRepository: DuaRepositoryProtocol {
    private struct QuranicEntry: Decodable {
        let surah: Int
        let ayah: Int
        let arabic: String
        let transliteration: String
        let translations: [String: String]
    }

    private struct HisnBook: Decodable {
        struct Category: Decodable {
            let id: String
            let title: String
            let chapters: [Chapter]
        }
        struct Chapter: Decodable {
            let id: Int
            let title: String
            let titleArabic: String
            let duas: [Entry]
        }
        struct Entry: Decodable {
            let id: Int
            let arabic: String
            let transliteration: String
            let translation: String
            /// The published Urdu, where there is one, and whose it is
            let translationUrdu: String?
            let translationUrduCredit: String?
            let `repeat`: Int
            let reference: String
            let source: String
        }
        let categories: [Category]
    }

    private let bundle: Bundle
    private var quranic: [QuranicEntry]?
    private var hisn: HisnBook?

    init(bundle: Bundle = .main) {
        self.bundle = bundle
    }

    func duas(language: String) throws -> [Dua] {
        try loadQuranic().map { entry in
            Dua(
                surahNumber: entry.surah,
                ayahNumber: entry.ayah,
                arabic: entry.arabic,
                transliteration: entry.transliteration,
                translation: Self.translation(entry, language: language)
            )
        }
    }

    func categories(language: String) throws -> [DuaCategory] {
        let rabbana = try loadQuranic().map { entry in
            DuaEntry(
                id: "quran-\(entry.surah):\(entry.ayah)",
                arabic: entry.arabic,
                transliteration: entry.transliteration,
                translation: Self.translation(entry, language: language),
                repeatCount: 1,
                source: "Quran \(entry.surah):\(entry.ayah)",
                reference: "",
                translationCredit: entry.translations[language] == nil || language == "en"
                    ? "Saheeh International"
                    : "Fateh Muhammad Jalandhry"
            )
        }
        let quranCategory = DuaCategory(
            id: "rabbana",
            title: "Rabbana Duas",
            chapters: [DuaChapter(id: "rabbana", title: "Rabbana Duas", titleArabic: "أدعية من القرآن", entries: rabbana)]
        )

        let hisnCategories = try loadHisn().categories.map { category in
            DuaCategory(
                id: category.id,
                title: category.title,
                chapters: category.chapters.map { chapter in
                    DuaChapter(
                        id: "hisn-\(chapter.id)",
                        title: chapter.title,
                        titleArabic: chapter.titleArabic,
                        entries: chapter.duas.map { entry in
                            // Urdu where a published translation exists, otherwise the English
                            let urdu = language == "ur" ? entry.translationUrdu.map { ($0, entry.translationUrduCredit) } : nil
                            return DuaEntry(
                                id: "hisn-\(entry.id)",
                                arabic: entry.arabic,
                                transliteration: entry.transliteration,
                                translation: urdu?.0 ?? entry.translation,
                                repeatCount: max(entry.`repeat`, 1),
                                source: entry.source,
                                reference: entry.reference,
                                translationCredit: (urdu?.1 ?? nil) ?? "Hisn al-Muslim (hisnmuslim.com)"
                            )
                        }
                    )
                }
            )
        }
        return [quranCategory] + hisnCategories
    }

    // Hindi isn't included: that edition's verse numbering drifts in places, so it falls back to English
    private static func translation(_ entry: QuranicEntry, language: String) -> String {
        entry.translations[language] ?? entry.translations["en"] ?? ""
    }

    private func loadQuranic() throws -> [QuranicEntry] {
        if let quranic { return quranic }
        let loaded = try decode([QuranicEntry].self, from: "Duas")
        quranic = loaded
        return loaded
    }

    private func loadHisn() throws -> HisnBook {
        if let hisn { return hisn }
        let loaded = try decode(HisnBook.self, from: "HisnAlMuslim")
        hisn = loaded
        return loaded
    }

    private func decode<T: Decodable>(_ type: T.Type, from resource: String) throws -> T {
        guard let url = bundle.url(forResource: resource, withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        return try JSONDecoder().decode(type, from: Data(contentsOf: url))
    }
}
