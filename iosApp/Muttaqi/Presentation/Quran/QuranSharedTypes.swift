import Foundation
import Observation
import Shared

// The Quran's types come from the shared code now. Home still shows a surah, an ayah and where the reader left off
// with the Swift structs of the same names, so these hand the shared values over to them; they can go once Home moves
// to shared code too.

extension Surah {
    init(_ surah: Shared.Surah) {
        self.init(
            id: Int(surah.number),
            number: Int(surah.number),
            name: surah.name,
            englishName: surah.englishName,
            englishNameTranslation: surah.englishNameTranslation,
            revelationType: surah.revelationType,
            numberOfAyahs: Int(surah.numberOfAyahs)
        )
    }
}

extension Shared.Surah {
    /// A surah opened from Home, shown in the reader's header until the reader has loaded it
    convenience init(_ surah: Surah) {
        self.init(
            number: Int32(surah.number),
            name: surah.name,
            englishName: surah.englishName,
            englishNameTranslation: surah.englishNameTranslation,
            revelationType: surah.revelationType,
            numberOfAyahs: Int32(surah.numberOfAyahs)
        )
    }
}

extension Ayah {
    init(_ ayah: Shared.Ayah) {
        self.init(
            id: Int(ayah.number),
            number: Int(ayah.number),
            numberInSurah: Int(ayah.numberInSurah),
            surahNumber: Int(ayah.surahNumber),
            arabicText: ayah.arabicText,
            transliteration: ayah.transliteration,
            translation: ayah.translation,
            juz: Int(ayah.juz),
            page: Int(ayah.page),
            hizbQuarter: Int(ayah.hizbQuarter)
        )
    }
}

extension DailyAyah {
    init(_ dailyAyah: Shared.DailyAyah) {
        self.init(surah: Surah(dailyAyah.surah), ayah: Ayah(dailyAyah.ayah))
    }
}

extension ReadingProgress {
    init(_ progress: Shared.ReadingProgress) {
        self.init(
            surahNumber: Int(progress.surahNumber),
            surahName: progress.surahName,
            surahEnglishName: progress.surahEnglishName,
            lastAyahNumber: Int(progress.lastAyahNumber),
            lastReadAt: Date(timeIntervalSince1970: TimeInterval(progress.lastReadAt.toEpochMilliseconds()) / 1000)
        )
    }
}

/// A shared value that changes many times a second, like the reading position while scrolling, observed on its own so
/// only the view that shows it redraws
@Observable
@MainActor
final class SharedValue<Value> {
    private(set) var value: Value?

    @ObservationIgnored private var observation: Task<Void, Never>?

    init(_ flow: SkieSwiftOptionalStateFlow<Value>) {
        value = flow.value
        observation = Task { [weak self] in
            for await next in flow {
                self?.value = next
            }
        }
    }

    isolated deinit {
        observation?.cancel()
    }
}
