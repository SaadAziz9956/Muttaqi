import Foundation

@Observable
@MainActor
final class EmotionsViewModel {
    enum Section: String, CaseIterable, Identifiable {
        case quran = "Quran"
        case hadith = "Hadith"
        case dua = "Dua"

        var id: Self { self }
    }

    private(set) var emotions: [Emotion] = []
    private(set) var header: EmotionsHeader?
    var selectedID: Emotion.ID?
    var section: Section = .quran

    private let getEmotions: GetEmotionsUseCase

    init(getEmotions: GetEmotionsUseCase, selectedID: Emotion.ID? = nil) {
        self.getEmotions = getEmotions
        self.selectedID = selectedID
    }

    var selected: Emotion? {
        emotions.first { $0.id == selectedID } ?? emotions.first
    }

    /// The kinds of text the selected emotion has, e.g. no Dua for some
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

    /// Reloads each time a page appears, so a translation language picked in the reader is used straight away
    func load() {
        guard let loaded = try? getEmotions.execute() else { return }
        header = loaded.header
        emotions = loaded.emotions
        keepSectionAvailable()
    }

    func select(_ emotion: Emotion) {
        selectedID = emotion.id
        keepSectionAvailable()
    }

    // Stays on the same kind of text when moving to another emotion, unless that emotion has none of it
    private func keepSectionAvailable() {
        if !sections.contains(section), let first = sections.first { section = first }
    }
}
