import Foundation

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

    init(
        fetchSurahsUseCase: FetchSurahsUseCase,
        getLastReadingUseCase: GetLastReadingUseCase
    ) {
        self.fetchSurahsUseCase = fetchSurahsUseCase
        self.getLastReadingUseCase = getLastReadingUseCase
    }

    func send(_ intent: Intent) {
        switch intent {
        case .onAppear:
            guard state == .idle else { return }
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
