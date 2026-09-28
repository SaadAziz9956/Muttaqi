import Foundation

enum Prayer: String, CaseIterable, Sendable {
    case fajr
    case dhuhr
    case asr
    case maghrib
    case isha

    var name: String {
        switch self {
        case .fajr: "Fajr"
        case .dhuhr: "Dhuhr"
        case .asr: "Asr"
        case .maghrib: "Maghrib"
        case .isha: "Isha"
        }
    }
}

/// A single day's prayer times
struct DailyPrayerTimes: Equatable, Sendable {
    let fajr: Date
    let sunrise: Date
    let dhuhr: Date
    let asr: Date
    let maghrib: Date
    let isha: Date

    func time(of prayer: Prayer) -> Date {
        switch prayer {
        case .fajr: fajr
        case .dhuhr: dhuhr
        case .asr: asr
        case .maghrib: maghrib
        case .isha: isha
        }
    }
}

struct UpcomingPrayer: Equatable, Sendable {
    let prayer: Prayer
    let time: Date
}

/// Today's and tomorrow's times, so there is always a next prayer, even after Isha
struct PrayerSchedule: Equatable, Sendable {
    let today: DailyPrayerTimes
    let tomorrow: DailyPrayerTimes

    func nextPrayer(after date: Date) -> UpcomingPrayer? {
        let upcoming = [today, tomorrow].flatMap { day in
            Prayer.allCases.map { UpcomingPrayer(prayer: $0, time: day.time(of: $0)) }
        }
        return upcoming.first { $0.time > date }
    }
}
