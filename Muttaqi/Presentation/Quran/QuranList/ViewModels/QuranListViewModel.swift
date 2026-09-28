import SwiftUI

@Observable
@MainActor
final class QuranListViewModel {
    enum ViewState: Equatable {
        case idle
        case loading
        case loaded
        case error(String)
    }

    enum Intent {
        case onAppear
        case retry
        case surahTapped(Surah)
        case continueTapped
    }

    private(set) var state: ViewState = .idle
    private(set) var surahs: [Surah] = []
    private(set) var readingProgress: ReadingProgress?

    var onSurahSelected: ((Surah) -> Void)?
    var onContinueReading: ((Surah, Int) -> Void)?

    private let fetchSurahsUseCase: FetchSurahsUseCase
    private let getLastReadingUseCase: GetLastReadingUseCase
    private let languagePreferences: LanguagePreferences

    init(
        fetchSurahsUseCase: FetchSurahsUseCase,
        getLastReadingUseCase: GetLastReadingUseCase,
        languagePreferences: LanguagePreferences
    ) {
        self.fetchSurahsUseCase = fetchSurahsUseCase
        self.getLastReadingUseCase = getLastReadingUseCase
        self.languagePreferences = languagePreferences
    }

    /// The reader's translation language, for the page's hadith
    var language: String {
        languagePreferences.getSelectedLanguage().code
    }

    func send(_ intent: Intent) {
        switch intent {
        case .onAppear:
            // Coming back from a surah: the list is already loaded, only the reading position has moved
            guard state == .idle else { return refreshProgress() }
            loadData()
        case .retry:
            loadData()
        case .surahTapped(let surah):
            onSurahSelected?(surah)
        case .continueTapped:
            guard let progress = readingProgress,
                  let surah = surahs.first(where: { $0.number == progress.surahNumber }) else { return }
            onContinueReading?(surah, progress.lastAyahNumber)
        }
    }

    private func refreshProgress() {
        Task {
            guard let progress = try? await getLastReadingUseCase.execute(), progress != readingProgress else { return }
            withAnimation(.smooth) { readingProgress = progress }
        }
    }

    private func loadData() {
        state = .loading
        Task {
            do {
                async let surahsResult = fetchSurahsUseCase.execute()
                async let progressResult = getLastReadingUseCase.execute()

                surahs = try await surahsResult
                readingProgress = try await progressResult
                state = .loaded
            } catch {
                state = .error(error.localizedDescription)
            }
        }
    }
}
