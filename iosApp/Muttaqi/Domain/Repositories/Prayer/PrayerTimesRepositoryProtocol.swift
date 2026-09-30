import Foundation

protocol PrayerTimesRepositoryProtocol: Sendable {
    func prayerTimes(on date: Date, at coordinates: Coordinates) -> DailyPrayerTimes?
    func qiblaDirection(from coordinates: Coordinates) -> QiblaDirection
}
