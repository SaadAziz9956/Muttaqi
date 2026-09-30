import Foundation

struct GetPrayerScheduleUseCase: Sendable {
    private let repository: PrayerTimesRepositoryProtocol
    private let calendar: Calendar

    init(repository: PrayerTimesRepositoryProtocol, calendar: Calendar = .current) {
        self.repository = repository
        self.calendar = calendar
    }

    func execute(at coordinates: Coordinates, on date: Date = .now) -> PrayerSchedule? {
        guard let tomorrowDate = calendar.date(byAdding: .day, value: 1, to: date),
              let today = repository.prayerTimes(on: date, at: coordinates),
              let tomorrow = repository.prayerTimes(on: tomorrowDate, at: coordinates) else { return nil }
        return PrayerSchedule(today: today, tomorrow: tomorrow)
    }
}
