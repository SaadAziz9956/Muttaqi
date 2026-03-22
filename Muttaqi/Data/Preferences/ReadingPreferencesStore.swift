import Foundation

protocol ReadingModePreferences: Sendable {
    func getReadingMode() -> ReadingMode
    func setReadingMode(_ mode: ReadingMode)
}

protocol FontPreferences: Sendable {
    func getFontSize() -> FontSize
    func setFontSize(_ size: FontSize)
}

protocol LanguagePreferences: Sendable {
    func getSelectedLanguage() -> Language
    func setSelectedLanguage(_ language: Language)
    func getDownloadedLanguages() -> [Language]
    func markLanguageAsDownloaded(_ language: Language)
}

protocol QuranDataPreferences: Sendable {
    func isDataDownloaded() -> Bool
    func setDataDownloaded(_ downloaded: Bool)
}

protocol ReadingPreferences: ReadingModePreferences, FontPreferences, LanguagePreferences, QuranDataPreferences {}

final class ReadingPreferencesStore: ReadingPreferences {
    private let storage: KeyValueStorage

    init(storage: KeyValueStorage) {
        self.storage = storage
    }

    func getReadingMode() -> ReadingMode {
        guard let raw = storage.string(forKey: Keys.readingMode),
              let mode = ReadingMode(rawValue: raw) else {
            return .withTranslation
        }
        return mode
    }

    func setReadingMode(_ mode: ReadingMode) {
        storage.set(mode.rawValue, forKey: Keys.readingMode)
    }

    func getFontSize() -> FontSize {
        let value = storage.integer(forKey: Keys.fontSize)
        return value > 0 ? FontSize(value) : .default
    }

    func setFontSize(_ size: FontSize) {
        storage.set(size.value, forKey: Keys.fontSize)
    }

    func getSelectedLanguage() -> Language {
        guard let code = storage.string(forKey: Keys.selectedLanguage) else {
            return .english
        }
        return Language.from(code: code)
    }

    func setSelectedLanguage(_ language: Language) {
        storage.set(language.code, forKey: Keys.selectedLanguage)
    }

    func getDownloadedLanguages() -> [Language] {
        let codes = storage.stringArray(forKey: Keys.downloadedLanguages) ?? []
        return codes.map { Language.from(code: $0) }
    }

    func markLanguageAsDownloaded(_ language: Language) {
        var languages = getDownloadedLanguages()
        if !languages.contains(language) {
            languages.append(language)
            storage.set(languages.map(\.code), forKey: Keys.downloadedLanguages)
        }
    }

    func isDataDownloaded() -> Bool {
        storage.bool(forKey: Keys.dataDownloaded)
    }

    func setDataDownloaded(_ downloaded: Bool) {
        storage.set(downloaded, forKey: Keys.dataDownloaded)
    }

    private enum Keys {
        static let readingMode = "reading_mode"
        static let fontSize = "reading_font_size"
        static let selectedLanguage = "reading_selected_language"
        static let dataDownloaded = "quran_data_downloaded"
        static let downloadedLanguages = "downloaded_languages"
    }
}

// MARK: - Key-Value Storage

protocol KeyValueStorage: Sendable {
    func string(forKey key: String) -> String?
    func integer(forKey key: String) -> Int
    func bool(forKey key: String) -> Bool
    func stringArray(forKey key: String) -> [String]?
    func set(_ value: Any?, forKey key: String)
}

final class UserDefaultsStorage: KeyValueStorage {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func string(forKey key: String) -> String? {
        defaults.string(forKey: key)
    }

    func integer(forKey key: String) -> Int {
        defaults.integer(forKey: key)
    }

    func bool(forKey key: String) -> Bool {
        defaults.bool(forKey: key)
    }

    func stringArray(forKey key: String) -> [String]? {
        defaults.stringArray(forKey: key)
    }

    func set(_ value: Any?, forKey key: String) {
        defaults.set(value, forKey: key)
    }
}
