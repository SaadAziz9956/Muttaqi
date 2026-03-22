import Foundation

enum QuranEndpoint {
    private static let baseURL = "https://api.qurani.ai/gw/qh/v1"

    /// GET /v1/surah — all 114 surahs metadata
    case surahList

    /// GET /v1/quran/{edition} — entire Quran in one edition
    case fullQuran(edition: String)

    /// GET /v1/surah/{number}/{edition} — single surah with ayahs
    case surah(number: Int, edition: String)

    /// GET /v1/surah/{number}/editions/{editions} — multi-edition single surah
    case surahEditions(number: Int, editions: [String])

    /// GET /v1/edition — list all available editions
    case editions

    /// GET /v1/edition?language={lang} — editions filtered by language
    case editionsByLanguage(String)

    /// GET /v1/search/{keyword}?language={lang}&surahNumber={surah}&limit={limit}
    case search(keyword: String, language: String?, surahNumber: Int?, limit: Int?)

    var url: URL? {
        switch self {
        case .surahList:
            return URL(string: "\(Self.baseURL)/surah")

        case .fullQuran(let edition):
            return URL(string: "\(Self.baseURL)/quran/\(edition)")

        case .surah(let number, let edition):
            return URL(string: "\(Self.baseURL)/surah/\(number)/\(edition)")

        case .surahEditions(let number, let editions):
            let joined = editions.joined(separator: ",")
            return URL(string: "\(Self.baseURL)/surah/\(number)/editions/\(joined)")

        case .editions:
            return URL(string: "\(Self.baseURL)/edition")

        case .editionsByLanguage(let lang):
            return URL(string: "\(Self.baseURL)/edition?language=\(lang)")

        case .search(let keyword, let language, let surahNumber, let limit):
            var components = URLComponents(string: "\(Self.baseURL)/search/\(keyword)")
            var queryItems: [URLQueryItem] = []
            if let language { queryItems.append(.init(name: "language", value: language)) }
            if let surahNumber { queryItems.append(.init(name: "surahNumber", value: "\(surahNumber)")) }
            if let limit { queryItems.append(.init(name: "limit", value: "\(limit)")) }
            if !queryItems.isEmpty { components?.queryItems = queryItems }
            return components?.url
        }
    }
}
