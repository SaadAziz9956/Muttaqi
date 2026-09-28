import Adhan
import CoreLocation
import Foundation

/// Prayer times and Qibla direction calculated on-device with Adhan, so they work offline
final class AdhanPrayerTimesRepository: PrayerTimesRepositoryProtocol {
    private let parameters: CalculationParameters
    private let calendar: Calendar

    /// Defaults to the University of Islamic Sciences, Karachi method with Hanafi Asr
    init(method: CalculationMethod = .karachi, madhab: Madhab = .hanafi, calendar: Calendar = .current) {
        var parameters = method.params
        parameters.madhab = madhab
        self.parameters = parameters
        self.calendar = calendar
    }

    func prayerTimes(on date: Date, at coordinates: Coordinates) -> DailyPrayerTimes? {
        let day = calendar.dateComponents([.year, .month, .day], from: date)
        guard let times = PrayerTimes(
            coordinates: Adhan.Coordinates(latitude: coordinates.latitude, longitude: coordinates.longitude),
            date: day,
            calculationParameters: parameters
        ) else { return nil }

        return DailyPrayerTimes(
            fajr: times.fajr,
            sunrise: times.sunrise,
            dhuhr: times.dhuhr,
            asr: times.asr,
            maghrib: times.maghrib,
            isha: times.isha
        )
    }

    func qiblaDirection(from coordinates: Coordinates) -> QiblaDirection {
        let bearing = Qibla(coordinates: Adhan.Coordinates(latitude: coordinates.latitude, longitude: coordinates.longitude)).direction
        let here = CLLocation(latitude: coordinates.latitude, longitude: coordinates.longitude)
        let kaaba = CLLocation(latitude: 21.4225, longitude: 39.8262)
        return QiblaDirection(bearing: bearing, distanceInKilometers: here.distance(from: kaaba) / 1000)
    }
}
