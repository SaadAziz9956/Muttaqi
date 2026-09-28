import SwiftUI

struct NextPrayerPill: View {
    let upcoming: UpcomingPrayer?
    let locationState: HomeViewModel.LocationState
    let onSetLocation: () -> Void

    var body: some View {
        if let upcoming {
            HStack(spacing: 10) {
                Text(upcoming.prayer.name)
                Text(upcoming.time, format: .dateTime.hour().minute())
            }
            .pillStyle()
            .accessibilityElement(children: .combine)
            .accessibilityLabel("Next prayer, \(upcoming.prayer.name) at \(upcoming.time.formatted(date: .omitted, time: .shortened))")
        } else if locationState == .needsPermission || locationState == .denied {
            // Without a location there are no times to show, so the pill becomes the way to set one
            Button("Set location", action: onSetLocation)
                .pillStyle()
                .accessibilityHint("Prayer times need your location")
        }
    }
}

private extension View {
    func pillStyle() -> some View {
        font(.custom("ReemKufi-Regular", size: 12))
            .foregroundStyle(.brandTeal)
            .padding(.horizontal, 12)
            .frame(height: 28)
            .background(.tintedSurface, in: .rect(cornerRadius: 8))
    }
}
