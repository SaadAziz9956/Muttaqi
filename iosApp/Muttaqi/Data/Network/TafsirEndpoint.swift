import Foundation

enum TafsirEndpoint {
    private static let baseURL = "https://api.quran.com/api/v4"
    // The API pages 10 ayahs at a time by default; the longest surah has 286, so one page of 300 covers any surah
    private static let perPage = 300

    /// GET /tafsirs/{tafsirId}/by_chapter/{chapterNumber}?per_page=300
    case tafsirByChapter(tafsirId: Int, chapterNumber: Int)

    var url: URL? {
        switch self {
        case .tafsirByChapter(let tafsirId, let chapterNumber):
            var components = URLComponents(string: "\(Self.baseURL)/tafsirs/\(tafsirId)/by_chapter/\(chapterNumber)")
            components?.queryItems = [URLQueryItem(name: "per_page", value: "\(Self.perPage)")]
            return components?.url
        }
    }
}
