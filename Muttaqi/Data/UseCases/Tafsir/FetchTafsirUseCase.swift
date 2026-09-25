import Foundation

struct FetchTafsirUseCase: Sendable {
    private let repository: TafsirRepositoryProtocol

    init(repository: TafsirRepositoryProtocol) {
        self.repository = repository
    }

    func execute(surahNumber: Int, language: Language) async throws -> [TafsirAyah] {
        try await repository.getTafsir(surahNumber: surahNumber, languageCode: language.code)
    }
}
