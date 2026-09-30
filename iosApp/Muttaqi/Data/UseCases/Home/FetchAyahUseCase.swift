import Foundation
import Shared

/// Fetches one ayah, with its surah, in the reader's translation language, from the shared Quran, for Home's verse
/// under the greeting and its Ayah of the Day
struct FetchAyahUseCase: Sendable {
    func execute(surahNumber: Int, ayahNumber: Int) async throws -> DailyAyah? {
        try await QuranUseCases.shared.ayah(surahNumber: Int32(surahNumber), ayahNumber: Int32(ayahNumber)).map(DailyAyah.init)
    }
}
