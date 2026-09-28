import Foundation

@Observable
@MainActor
final class DhikrListViewModel {
    private(set) var sections: [DhikrSection] = []
    /// The reader's translation language, for the page's hadith
    private(set) var language = "en"
    var selectedSectionID: DhikrSection.ID?

    private let getSections: GetDhikrSectionsUseCase
    private let languagePreferences: LanguagePreferences

    init(getSections: GetDhikrSectionsUseCase, languagePreferences: LanguagePreferences) {
        self.getSections = getSections
        self.languagePreferences = languagePreferences
    }

    /// The tab being shown: the one picked, or the first
    var selectedSection: DhikrSection? {
        sections.first { $0.id == selectedSectionID } ?? sections.first
    }

    /// Reloads each time the page appears, so a translation language picked in the reader is used straight away
    func load() {
        language = languagePreferences.getSelectedLanguage().code
        sections = (try? getSections.execute()) ?? []
    }
}
