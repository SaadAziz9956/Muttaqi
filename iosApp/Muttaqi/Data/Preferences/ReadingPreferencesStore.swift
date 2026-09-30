import Foundation

protocol LanguagePreferences: Sendable {
    func getSelectedLanguage() -> Language
    func setSelectedLanguage(_ language: Language)
}

/// The reader's translation language, for the Swift features still to move to shared code. The Quran's reading mode,
/// font size and download notes are in the shared code now, under the same keys; the language is stored under the key
/// the shared code reads too, so both always agree
final class ReadingPreferencesStore: LanguagePreferences {
    private let storage: KeyValueStorage

    init(storage: KeyValueStorage) {
        self.storage = storage
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

    private enum Keys {
        static let selectedLanguage = "reading_selected_language"
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
