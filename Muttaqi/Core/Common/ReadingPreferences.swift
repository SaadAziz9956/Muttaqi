import Foundation

// MARK: - Protocol
protocol ReadingPreferencesProtocol: Sendable {
    func getReadingMode() -> ReadingMode
    func setReadingMode(_ mode: ReadingMode)
    func getFontSize() -> Int
    func setFontSize(_ size: Int)
    func getSelectedLanguage() -> String
    func setSelectedLanguage(_ language: String)
    func isDataDownloaded() -> Bool
    func setDataDownloaded(_ downloaded: Bool)
    func getDownloadedLanguages() -> [String]
    func addDownloadedLanguage(_ language: String)
}

// MARK: - Implementation
final class ReadingPreferences: ReadingPreferencesProtocol, @unchecked Sendable {
    private let defaults: UserDefaults

    private enum Keys {
        static let readingMode = "reading_mode"
        static let fontSize = "reading_font_size"
        static let selectedLanguage = "reading_selected_language"
        static let dataDownloaded = "quran_data_downloaded"
        static let downloadedLanguages = "downloaded_languages"
    }

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func getReadingMode() -> ReadingMode {
        guard let raw = defaults.string(forKey: Keys.readingMode),
              let mode = ReadingMode(rawValue: raw) else {
            return .withTranslation
        }
        return mode
    }

    func setReadingMode(_ mode: ReadingMode) {
        defaults.set(mode.rawValue, forKey: Keys.readingMode)
    }

    func getFontSize() -> Int {
        let size = defaults.integer(forKey: Keys.fontSize)
        return size > 0 ? size : 16
    }

    func setFontSize(_ size: Int) {
        defaults.set(size, forKey: Keys.fontSize)
    }

    func getSelectedLanguage() -> String {
        defaults.string(forKey: Keys.selectedLanguage) ?? "en"
    }

    func setSelectedLanguage(_ language: String) {
        defaults.set(language, forKey: Keys.selectedLanguage)
    }

    func isDataDownloaded() -> Bool {
        defaults.bool(forKey: Keys.dataDownloaded)
    }

    func setDataDownloaded(_ downloaded: Bool) {
        defaults.set(downloaded, forKey: Keys.dataDownloaded)
    }

    func getDownloadedLanguages() -> [String] {
        defaults.stringArray(forKey: Keys.downloadedLanguages) ?? []
    }

    func addDownloadedLanguage(_ language: String) {
        var languages = getDownloadedLanguages()
        if !languages.contains(language) {
            languages.append(language)
            defaults.set(languages, forKey: Keys.downloadedLanguages)
        }
    }
}
