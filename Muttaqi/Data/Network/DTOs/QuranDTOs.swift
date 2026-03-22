import Foundation

// MARK: - Base API Response Wrapper
/// All qurani.ai responses follow: {"code": 200, "status": "OK", "data": ...}
struct APIResponse<T: Decodable>: Decodable {
    let code: Int
    let status: String
    let data: T
}

// MARK: - Surah List
struct SurahDTO: Decodable {
    let number: Int
    let name: String
    let englishName: String
    let englishNameTranslation: String
    let numberOfAyahs: Int
    let revelationType: String
}

// MARK: - Full Quran / Single Surah with Ayahs
struct SurahDetailDTO: Decodable {
    let number: Int
    let name: String
    let englishName: String
    let englishNameTranslation: String
    let numberOfAyahs: Int
    let revelationType: String
    let ayahs: [AyahDTO]
    let edition: EditionDTO?
}

struct AyahDTO: Decodable {
    let number: Int
    let text: String
    let numberInSurah: Int
    let juz: Int
    let manzil: Int
    let page: Int
    let ruku: Int
    let hizbQuarter: Int
    let surah: SurahRefDTO?
}

struct SurahRefDTO: Decodable {
    let number: Int
    let name: String
    let englishName: String
    let englishNameTranslation: String
    let numberOfAyahs: Int
    let revelationType: String
}

struct EditionDTO: Decodable {
    let identifier: String
    let language: String
    let name: String
    let englishName: String
    let format: String
    let type: String
    let direction: String?
}

// MARK: - Full Quran Response
/// /v1/quran/{edition} returns { "data": { "surahs": [...], "edition": {...} } }
struct FullQuranDTO: Decodable {
    let surahs: [SurahDetailDTO]
    let edition: EditionDTO
}

// MARK: - Edition List
/// /v1/edition returns { "data": [ EditionDTO ] }
typealias EditionListResponse = APIResponse<[EditionDTO]>
