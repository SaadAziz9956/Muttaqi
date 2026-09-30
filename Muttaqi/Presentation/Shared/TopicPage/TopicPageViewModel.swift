import Foundation

/// A page of Quran verses, hadith and duas for one topic, with tabs to move between it and its neighbours, e.g. the
/// emotions, or the topics in one Explore group
@Observable
@MainActor
final class TopicPageViewModel<Topic: PassageTopic> {
    enum Section: String, CaseIterable, Identifiable {
        case quran = "Quran"
        case hadith = "Hadith"
        case dua = "Dua"

        var id: Self { self }
    }

    /// The topics in the tabs
    private(set) var topics: [Topic] = []
    var selectedID: Topic.ID?
    var section: Section = .quran

    private let loadTopics: @MainActor () throws -> [Topic]

    /// `loadTopics` gives the topics in the reader's current translation language
    init(selectedID: Topic.ID?, loadTopics: @escaping @MainActor () throws -> [Topic]) {
        self.selectedID = selectedID
        self.loadTopics = loadTopics
    }

    var selected: Topic? {
        topics.first { $0.id == selectedID } ?? topics.first
    }

    /// The kinds of text the selected topic has, e.g. no Dua for some
    var sections: [Section] {
        guard let selected else { return [] }
        return Section.allCases.filter { section in
            switch section {
            case .quran: !selected.verses.isEmpty
            case .hadith: !selected.hadith.isEmpty
            case .dua: !selected.duas.isEmpty
            }
        }
    }

    /// Reloads each time the page appears, so a translation language picked in the reader is used straight away
    func load() {
        guard let loaded = try? loadTopics() else { return }
        topics = loaded
        keepSectionAvailable()
    }

    func select(_ topic: Topic) {
        selectedID = topic.id
        keepSectionAvailable()
    }

    // Stays on the same kind of text when moving to another topic, unless that topic has none of it
    private func keepSectionAvailable() {
        if !sections.contains(section), let first = sections.first { section = first }
    }
}
