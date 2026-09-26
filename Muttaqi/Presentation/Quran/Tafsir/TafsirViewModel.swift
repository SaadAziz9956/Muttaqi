import Foundation

@Observable
@MainActor
final class TafsirViewModel {
    enum State {
        case idle
        case loading
        case loaded([TafsirAyah])
        case error(String)
    }

    var state: State = .idle

    private let fetchTafsir: FetchTafsirUseCase

    init(fetchTafsir: FetchTafsirUseCase) {
        self.fetchTafsir = fetchTafsir
    }

    func load(surahNumber: Int, language: Language) async {
        state = .loading
        do {
            let ayahs = try await fetchTafsir.execute(surahNumber: surahNumber, language: language)
            state = .loaded(ayahs)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
