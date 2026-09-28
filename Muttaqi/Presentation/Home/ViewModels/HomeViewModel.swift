import Foundation

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

    private let location: LocationRepositoryProtocol
    private let getPrayerSchedule: GetPrayerScheduleUseCase
    private let fetchAyah: FetchAyahUseCase
    private let getAyahOfTheDay: GetAyahOfTheDayUseCase
    private let getDuaOfTheDay: GetDuaOfTheDayUseCase
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
        calendar: Calendar = .current
    ) {
        self.location = location
        self.getPrayerSchedule = getPrayerSchedule
        self.fetchAyah = fetchAyah
        self.getAyahOfTheDay = getAyahOfTheDay
        self.getDuaOfTheDay = getDuaOfTheDay
        self.calendar = calendar
    }

    /// Called on appear and whenever the app returns to the foreground, so the day's content and times stay current
    func refresh() async {
        quote = try? await fetchAyah.execute(surahNumber: 3, ayahNumber: 139)
        ayahOfTheDay = try? await getAyahOfTheDay.execute()
        duaOfTheDay = try? getDuaOfTheDay.execute()
        await loadPrayerTimes()
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
