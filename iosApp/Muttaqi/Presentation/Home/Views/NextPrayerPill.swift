import Shared
import SwiftUI

struct NextPrayerPill: View {
    let upcoming: UpcomingPrayer?
    let asksForLocation: Bool
    let onSetLocation: () -> Void

    var body: some View {
        if let upcoming {
            let date = Date(upcoming.time)
            HStack(spacing: 10) {
                Text(upcoming.prayer.displayName)
                Text(date, format: .dateTime.hour().minute())
            }
            .pillStyle()
            .accessibilityElement(children: .combine)
            .accessibilityLabel("Next prayer, \(upcoming.prayer.displayName) at \(date.formatted(date: .omitted, time: .shortened))")
        } else if asksForLocation {
            Button("Set location", action: onSetLocation)
                .pillStyle()
                .accessibilityHint("Prayer times need your location")
        }
    }
}

private extension View {
    func pillStyle() -> some View {
        font(.custom("ReemKufi-Medium", size: 13))
            .foregroundStyle(.appPrimary)
            .softPill()
    }
}
