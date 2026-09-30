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

    /// Prayer times, the Qibla, the compass and location, from the shared code
    private let prayer: HomePrayerTimes
    private let fetchAyah: FetchAyahUseCase
    private let getAyahOfTheDay: GetAyahOfTheDayUseCase
    private let getDuaOfTheDay: GetDuaOfTheDayUseCase
    private let exploreOfTheDay: ExploreOfTheDay
    private let pickNameOfTheDay: NameOfTheDay
    private let dhikrSaidToday: DhikrSaidToday
    private let quran: QuranUseCases
    /// Today's journal entry, from the shared journal
    let journal: SharedViewModel<JournalTodayViewModel, JournalTodayState>
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
        fetchAyah: FetchAyahUseCase,
        getAyahOfTheDay: GetAyahOfTheDayUseCase,
        getDuaOfTheDay: GetDuaOfTheDayUseCase,
        quran: QuranUseCases,
        journal: SharedViewModel<JournalTodayViewModel, JournalTodayState>,
        prayer: HomePrayerTimes = .shared,
        exploreOfTheDay: ExploreOfTheDay = .shared,
        nameOfTheDay: NameOfTheDay = .shared,
        dhikrSaidToday: DhikrSaidToday = .shared,
        calendar: Calendar = .current
    ) {
        self.prayer = prayer
        self.fetchAyah = fetchAyah
        self.getAyahOfTheDay = getAyahOfTheDay
        self.getDuaOfTheDay = getDuaOfTheDay
        self.quran = quran
        self.journal = journal
        self.exploreOfTheDay = exploreOfTheDay
        self.pickNameOfTheDay = nameOfTheDay
        self.dhikrSaidToday = dhikrSaidToday
        self.calendar = calendar
    }

    /// Called on appear and whenever the app returns to the foreground, so the day's content and times stay current
    func refresh() async {
        quote = try? await fetchAyah.execute(surahNumber: 3, ayahNumber: 139)
        ayahOfTheDay = try? await getAyahOfTheDay.execute()
        duaOfTheDay = try? getDuaOfTheDay.execute()
        await loadDailyPicks()
        dhikrToday = (try? await dhikrSaidToday.count().intValue) ?? 0
        journal.viewModel.dispatch(intent: JournalTodayIntentRefresh.shared)
        await loadLastReading()
        await loadPrayerTimes()
        if let coordinates = prayer.lastKnownCoordinates() {
            qibla = prayer.qibla(coordinates: coordinates)
        }
    }

    /// Today's journal entry, if one has been written
    var journalToday: JournalEntry? {
        journal.state.entry
    }

    func isFriday(_ date: Date) -> Bool {
        calendar.component(.weekday, from: date) == 6
    }

    /// Follows the compass for the Qibla tile until the calling task is cancelled
    func trackQibla() async {
        guard prayer.isCompassAvailable else { return }
        var previous: Double?
        for await update in prayer.headings() {
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

    // One of each, changing at midnight: a Name of Allah, an Explore topic and an authentic hadith from Explore, all
    // picked by the shared code as the iOS app always has
    private func loadDailyPicks() async {
        if let name = try? await pickNameOfTheDay.name() { nameOfTheDay = name }
        if let topic = try? await exploreOfTheDay.topic() { topicOfTheDay = topic }
        if let hadith = try? await exploreOfTheDay.hadith() { hadithOfTheDay = hadith }
    }

    private func loadLastReading() async {
        guard let surahs = try? await quran.surahs().map(Surah.init) else { return }
        kahf = surahs.first { $0.number == 18 }
        if let progress = try? await quran.lastReading().map(ReadingProgress.init),
           let surah = surahs.first(where: { $0.number == progress.surahNumber }) {
            lastReading = (progress, surah)
        }
    }

    func requestLocation() async {
        switch try? await prayer.requestAccess() {
        case .granted: await loadPrayerTimes()
        case .denied: locationState = .denied
        case .notDetermined, nil: locationState = .needsPermission
        }
    }

    func nextPrayer(at date: Date) -> UpcomingPrayer? {
        schedule?.nextPrayer(afterDate: date)
    }

    /// The Islamic day begins at Maghrib, so after sunset this shows the next Hijri date
    func hijriDate(at date: Date) -> String {
        var day = date
        if let maghrib = schedule?.today.date(prayer: .maghrib), calendar.isDate(maghrib, inSameDayAs: date), date >= maghrib {
            day = calendar.date(byAdding: .day, value: 1, to: date) ?? date
        }
        return Self.hijriFormatter.string(from: day)
    }

    private func loadPrayerTimes() async {
        // Saved coordinates give times instantly and offline; a fresh fix then corrects them if the user has moved
        if let saved = prayer.lastKnownCoordinates() {
            schedule = prayer.schedule(coordinates: saved)
        }
        switch prayer.access {
        case .granted:
            if let fresh = try? await prayer.refreshCoordinates() {
                schedule = prayer.schedule(coordinates: fresh)
            }
            locationState = schedule == nil ? .unknown : .available
        case .denied:
            locationState = schedule == nil ? .denied : .available
        case .notDetermined:
            locationState = schedule == nil ? .needsPermission : .available
        }
    }
}
