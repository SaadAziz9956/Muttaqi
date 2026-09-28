import Foundation

/// Picks the same ayah all day from a curated list, moving to the next one at midnight
struct GetAyahOfTheDayUseCase: Sendable {
    /// Well-known ayahs that read clearly on their own, rather than any random ayah out of its context
    static let curatedAyahs: [(surah: Int, ayah: Int)] = [
        (2, 45), (2, 152), (2, 153), (2, 186), (2, 286), (3, 31), (3, 92), (3, 159), (3, 185), (3, 200),
        (6, 162), (7, 56), (8, 46), (9, 51), (11, 115), (13, 11), (13, 28), (14, 7), (16, 18), (16, 97),
        (16, 128), (21, 35), (25, 63), (29, 69), (30, 21), (39, 53), (40, 60), (41, 34), (47, 7), (49, 10),
        (49, 13), (50, 16), (51, 56), (55, 13), (64, 11), (65, 3), (93, 3), (93, 5), (94, 6),
    ]

    private let fetchAyah: FetchAyahUseCase
    private let calendar: Calendar

    init(fetchAyah: FetchAyahUseCase, calendar: Calendar = .current) {
        self.fetchAyah = fetchAyah
        self.calendar = calendar
    }

    func execute(on date: Date = .now) async throws -> DailyAyah? {
        let day = calendar.ordinality(of: .day, in: .era, for: date) ?? 0
        let pick = Self.curatedAyahs[day % Self.curatedAyahs.count]
        return try await fetchAyah.execute(surahNumber: pick.surah, ayahNumber: pick.ayah)
    }
}
