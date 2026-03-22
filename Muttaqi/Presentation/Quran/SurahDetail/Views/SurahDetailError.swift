import Foundation

/// Domain-specific errors for Surah detail feature
enum SurahDetailError: LocalizedError, Equatable {
    case failedToLoadSurah(surahNumber: Int, underlyingError: String)
    case failedToLoadAyahs(surahNumber: Int, underlyingError: String)
    case failedToDownloadLanguage(language: Language, underlyingError: String)
    case invalidSurahNumber(Int)
    
    var errorDescription: String? {
        switch self {
        case .failedToLoadSurah(let number, _):
            return "Unable to load Surah \(number). Please check your connection and try again."
        case .failedToLoadAyahs(let number, _):
            return "Unable to load verses for Surah \(number). Please try again."
        case .failedToDownloadLanguage(let language, _):
            return "Failed to download \(language.name) translation. Please check your connection."
        case .invalidSurahNumber(let number):
            return "Invalid Surah number: \(number). Please select a valid Surah (1-114)."
        }
    }
    
    var recoverySuggestion: String? {
        switch self {
        case .failedToLoadSurah, .failedToLoadAyahs:
            return "Make sure you have an internet connection and the Quran data is downloaded."
        case .failedToDownloadLanguage:
            return "Check your internet connection and available storage."
        case .invalidSurahNumber:
            return "Please navigate to a valid Surah."
        }
    }
}
