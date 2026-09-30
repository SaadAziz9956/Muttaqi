import Foundation

/// The list of emotions; each opens as a `TopicPageView`
@Observable
@MainActor
final class EmotionsViewModel {
    private(set) var emotions: [Emotion] = []
    private(set) var header: PageQuote?

    private let getEmotions: GetEmotionsUseCase

    init(getEmotions: GetEmotionsUseCase) {
        self.getEmotions = getEmotions
    }

    /// Reloads each time the page appears, so a translation language picked in the reader is used straight away
    func load() {
        guard let loaded = try? getEmotions.execute() else { return }
        header = loaded.header
        emotions = loaded.emotions
    }
}
