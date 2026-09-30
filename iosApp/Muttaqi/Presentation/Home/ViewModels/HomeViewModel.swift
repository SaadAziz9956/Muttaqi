import Foundation
import Shared

@Observable
@MainActor
final class HomeViewModel {
    enum LocationState: Equatable {
        case unknown
        case needsPermission
        case denied
        case available
    }

    private(set) var schedule: PrayerSchedule?
    private(set) var locationState: LocationState = .unknown
    private(set) var quote: DailyAyah?
    private(set) var ayahOfTheDay: DailyAyah?
    private(set) var duaOfTheDay: Dua?
    private(set) var hadithOfTheDay: HadithPassage?
    private(set) var nameOfTheDay: AllahName?
    private(set) var topicOfTheDay: ExploreTopic?
    /// Where the reader left off in the Quran, and that surah, to open it again
    private(set) var lastReading: (progress: ReadingProgress, surah: Surah)?
    /// Surah al-Kahf, suggested on Fridays
    private(set) var kahf: Surah?
    /// Every dhikr said today, all counters together
    private(set) var dhikrToday = 0
    private(set) var qibla: QiblaDirection?
    /// Turn from the phone's heading to the Kaaba, as a continuous angle so the arrow turns the short way round
    private(set) var qiblaArrowRotation: Double?

    private let location: LocationRepositoryProtocol
    private let getPrayerSchedule: GetPrayerScheduleUseCase
    private let fetchAyah: FetchAyahUseCase
    private let getAyahOfTheDay: GetAyahOfTheDayUseCase
    private let getDuaOfTheDay: GetDuaOfTheDayUseCase
    private let exploreOfTheDay: ExploreOfTheDay
    private let getNames: GetAllahNamesUseCase
    private let getDhikrSections: GetDhikrSectionsUseCase
    private let dhikrProgress: DhikrProgressStoring
    private let getLastReading: GetLastReadingUseCase
    private let fetchSurahs: FetchSurahsUseCase
    private let getQiblaDirection: GetQiblaDirectionUseCase
    private let compass: CompassServiceProtocol
    let journal: JournalStore
    private let calendar: Calendar

    private static let hijriFormatter: DateFormatter = {
        var hijri = Calendar(identifier: .islamicUmmAlQura)
        hijri.locale = Locale(identifier: "en")
        let formatter = DateFormatter()
        formatter.calendar = hijri
        formatter.locale = Locale(identifier: "en")
        formatter.dateFormat = "MMMM d, y G"
        return formatter
    }()

    init(
        location: LocationRepositoryProtocol,
        getPrayerSchedule: GetPrayerScheduleUseCase,
        fetchAyah: FetchAyahUseCase,
        getAyahOfTheDay: GetAyahOfTheDayUseCase,
        getDuaOfTheDay: GetDuaOfTheDayUseCase,
        getNames: GetAllahNamesUseCase,
        getDhikrSections: GetDhikrSectionsUseCase,
        dhikrProgress: DhikrProgressStoring,
        getLastReading: GetLastReadingUseCase,
        fetchSurahs: FetchSurahsUseCase,
        getQiblaDirection: GetQiblaDirectionUseCase,
        compass: CompassServiceProtocol,
        journal: JournalStore,
        exploreOfTheDay: ExploreOfTheDay = .shared,
        calendar: Calendar = .current
    ) {
        self.location = location
        self.getPrayerSchedule = getPrayerSchedule
        self.fetchAyah = fetchAyah
        self.getAyahOfTheDay = getAyahOfTheDay
        self.getDuaOfTheDay = getDuaOfTheDay
        self.getNames = getNames
        self.getDhikrSections = getDhikrSections
        self.dhikrProgress = dhikrProgress
        self.getLastReading = getLastReading
        self.fetchSurahs = fetchSurahs
        self.getQiblaDirection = getQiblaDirection
        self.compass = compass
        self.journal = journal
        self.exploreOfTheDay = exploreOfTheDay
        self.calendar = calendar
    }

    /// Called on appear and whenever the app returns to the foreground, so the day's content and times stay current
    func refresh() async {
        quote = try? await fetchAyah.execute(surahNumber: 3, ayahNumber: 139)
        ayahOfTheDay = try? await getAyahOfTheDay.execute()
        duaOfTheDay = try? getDuaOfTheDay.execute()
        await loadDailyPicks()
        dhikrToday = countDhikrToday()
        await journal.load()
        await loadLastReading()
        await loadPrayerTimes()
        if let coordinates = location.lastKnownCoordinates() {
            qibla = getQiblaDirection.execute(from: coordinates)
        }
    }

    /// Today's journal entry, if one has been written
    var journalToday: JournalEntry? {
        journal.entries.first { calendar.isDateInToday($0.createdAt) }
    }

    func isFriday(_ date: Date) -> Bool {
        calendar.component(.weekday, from: date) == 6
    }

    /// Follows the compass for the Qibla tile until the calling task is cancelled
    func trackQibla() async {
        guard compass.isAvailable else { return }
        var previous: Double?
        for await update in compass.headings() {
            guard let qibla else { continue }
            let target = qibla.bearing - update.degrees
            if let previous, let current = qiblaArrowRotation {
                var step = (target - previous).truncatingRemainder(dividingBy: 360)
                if step > 180 { step -= 360 } else if step < -180 { step += 360 }
                qiblaArrowRotation = current + step
            } else {
                qiblaArrowRotation = target
            }
            previous = target
        }
    }

    // One of each, changing at midnight: a Name of Allah, an Explore topic and an authentic hadith from Explore
    private func loadDailyPicks() async {
        let day = calendar.ordinality(of: .day, in: .era, for: .now) ?? 0
        if let names = try? getNames.execute(), !names.isEmpty {
            nameOfTheDay = names[day % names.count]
        }
        // The Explore topic and hadith, picked by the shared code in the same way
        if let topic = try? await exploreOfTheDay.topic() { topicOfTheDay = topic }
        if let hadith = try? await exploreOfTheDay.hadith() { hadithOfTheDay = hadith }
    }

    private func countDhikrToday() -> Int {
        guard let sections = try? getDhikrSections.execute() else { return 0 }
        return sections.flatMap(\.dhikr).reduce(0) { total, dhikr in
            let progress = dhikrProgress.progress(for: dhikr.id)
            return total + progress.count + progress.rounds * (dhikr.target ?? 0)
        }
    }

    private func loadLastReading() async {
        guard let surahs = try? await fetchSurahs.execute() else { return }
        kahf = surahs.first { $0.number == 18 }
        if let progress = try? await getLastReading.execute(),
           let surah = surahs.first(where: { $0.number == progress.surahNumber }) {
            lastReading = (progress, surah)
        }
    }

    func requestLocation() async {
        switch await location.requestAccess() {
        case .granted: await loadPrayerTimes()
        case .denied: locationState = .denied
        case .notDetermined: locationState = .needsPermission
        }
    }

    func nextPrayer(at date: Date) -> UpcomingPrayer? {
        schedule?.nextPrayer(after: date)
    }

    /// The Islamic day begins at Maghrib, so after sunset this shows the next Hijri date
    func hijriDate(at date: Date) -> String {
        var day = date
        if let maghrib = schedule?.today.maghrib, calendar.isDate(maghrib, inSameDayAs: date), date >= maghrib {
            day = calendar.date(byAdding: .day, value: 1, to: date) ?? date
        }
        return Self.hijriFormatter.string(from: day)
    }

    private func loadPrayerTimes() async {
        // Saved coordinates give times instantly and offline; a fresh fix then corrects them if the user has moved
        if let saved = location.lastKnownCoordinates() {
            schedule = getPrayerSchedule.execute(at: saved)
        }
        switch location.access {
        case .granted:
            if let fresh = await location.refreshCoordinates() {
                schedule = getPrayerSchedule.execute(at: fresh)
            }
            locationState = schedule == nil ? .unknown : .available
        case .denied:
            locationState = schedule == nil ? .denied : .available
        case .notDetermined:
            locationState = schedule == nil ? .needsPermission : .available
        }
    }
}
