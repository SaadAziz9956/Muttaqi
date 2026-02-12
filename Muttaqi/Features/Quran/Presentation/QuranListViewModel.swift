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
        case surahTapped(Int)
        case continueTapped
    }

    private(set) var state: ViewState = .idle
    private(set) var surahs: [Surah] = []
    private(set) var readingProgress: ReadingProgress?

    var onSurahSelected: ((Int) -> Void)?
    var onContinueReading: ((Int, Int) -> Void)?

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
        case .surahTapped(let number):
            onSurahSelected?(number)
        case .continueTapped:
            guard let progress = readingProgress else { return }
            onContinueReading?(progress.surahNumber, progress.lastAyahNumber)
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
