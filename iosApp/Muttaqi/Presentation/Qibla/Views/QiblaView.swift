import Shared
import SwiftUI

struct QiblaView: View {
    @State private var screen = SharedViewModel(PrayerViewModels.shared.qibla()) { $0.state }
    @State private var reading: QiblaCompass?
    @State private var timesFacingQibla = 0
    @Environment(\.openURL) private var openURL

    private var state: QiblaState { screen.state }

    var body: some View {
        ZStack {
            SoftBackdrop()

            switch onEnum(of: state.phase) {
            case .locating:
                ProgressView()
                    .tint(.appPrimary)
            case .needsLocation(let needs):
                locationNeeded(needs.access)
            case .ready(let ready):
                compass(ready.qibla)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Qibla")
                    .font(.custom("ReemKufi-Regular", size: 20))
                    .foregroundStyle(.appPrimary)
            }
        }
        .toolbar(.hidden, for: .tabBar)
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .facingQibla:
                    timesFacingQibla += 1
                case .openSettings:
                    if let settings = URL(string: UIApplication.openSettingsURLString) { openURL(settings) }
                }
            }
        }
        .task(id: state.qibla != nil) {
            if state.qibla != nil {
                for await next in screen.viewModel.compass { reading = next }
            }
        }
        .sensoryFeedback(.success, trigger: timesFacingQibla)
    }

    private func compass(_ qibla: QiblaDirection) -> some View {
        VStack(spacing: 0) {
            Spacer(minLength: 12)

            QiblaCompassDial(
                qiblaBearing: qibla.bearing,
                rotation: dialRotation,
                isAligned: isAligned
            )
            .frame(width: 300, height: 300)
            .animation(.easeOut(duration: 0.25), value: dialRotation)
            .animation(.snappy, value: isAligned)

            Text(instruction(for: qibla))
                .font(.custom("ReemKufi-Medium", size: 24))
                .foregroundStyle(.appPrimary)
                .contentTransition(.numericText())
                .padding(.top, 36)

            HStack(spacing: 14) {
                stat("Bearing", value: "\(Int(qibla.bearing.rounded()))°", detail: "from North")
                stat(
                    "Distance",
                    value: "\(qibla.distanceInKilometers.formatted(.number.precision(.fractionLength(0)))) km",
                    detail: "to Makkah"
                )
            }
            .padding(.top, 24)

            if let note {
                Text(note)
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(16)
                    .frame(maxWidth: .infinity)
                    .softCard(cornerRadius: 22)
                    .padding(.top, 14)
            }

            Spacer(minLength: 12)
        }
        .padding(.horizontal, 20)
        .accessibilityElement(children: .combine)
    }

    private var dialRotation: Double { reading?.dialRotation ?? 0 }

    private var isAligned: Bool { reading?.isAligned ?? false }

    private var note: String? {
        guard let qibla = state.qibla else { return nil }
        if !state.isCompassAvailable {
            return "This device has no compass. Use one to face \(Int(qibla.bearing.rounded()))° from North."
        }
        if reading?.needsCalibration ?? false {
            return "Move your phone in a figure-eight to calibrate the compass."
        }
        return nil
    }

    private func stat(_ label: String, value: String, detail: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.custom("ReemKufi-Regular", size: 12))
                .foregroundStyle(.textSecondary)
            Text(value)
                .font(.custom("ReemKufi-Medium", size: 22))
                .foregroundStyle(.appPrimary)
            Text(detail)
                .font(.custom("ReemKufi-Regular", size: 12))
                .foregroundStyle(.brandTeal)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .softCard(cornerRadius: 24)
    }

    private func instruction(for qibla: QiblaDirection) -> String {
        guard let turn = reading?.turnAngle else {
            return "Face \(Int(qibla.bearing.rounded()))° from North"
        }
        if isAligned { return "You're facing the Qibla" }
        return "Turn \(turn > 0 ? "right" : "left") \(Int(abs(turn).rounded()))°"
    }

    private func locationNeeded(_ access: LocationAccess) -> some View {
        ContentUnavailableView {
            Label {
                Text("Location needed")
            } icon: {
                Image("home-qibla")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 44, height: 44)
                    .foregroundStyle(.brandTeal)
            }
        } description: {
            Text("Muttaqi uses your location to find the direction of the Kaaba.")
        } actions: {
            Button(access == .denied ? "Open Settings" : "Allow Location") {
                screen.viewModel.dispatch(intent: QiblaIntentLocationButtonTapped.shared)
            }
            .buttonStyle(.glassProminent)
            .tint(.shareCard)
        }
    }
}

private struct QiblaCompassDial: View {
    let qiblaBearing: Double
    let rotation: Double
    let isAligned: Bool

    var body: some View {
        GeometryReader { geometry in
            let radius = min(geometry.size.width, geometry.size.height) / 2

            ZStack {
                Circle()
                    .fill(Color.softSurface)
                    .overlay { Circle().strokeBorder(Color.softRim, lineWidth: 2) }
                    .shadow(color: .softShadow, radius: 26, y: 14)
                    .shadow(color: .softShadow.opacity(0.5), radius: 2, y: 1)

                Circle()
                    .strokeBorder(Color.shareCard, lineWidth: 3)
                    .shadow(color: Color.shareCard.opacity(0.45), radius: 16)
                    .opacity(isAligned ? 1 : 0)

                ZStack {
                    Circle()
                        .fill(.tintedSurface)
                        .padding(22)

                    ForEach(0..<72, id: \.self) { tick in
                        let isMajor = tick % 6 == 0
                        Capsule()
                            .fill(isMajor ? Color.appPrimary : Color.textSecondary.opacity(0.45))
                            .frame(width: isMajor ? 2 : 1, height: isMajor ? 12 : 6)
                            .offset(y: -radius + 12)
                            .rotationEffect(.degrees(Double(tick) * 5))
                    }

                    ForEach(Array(["N", "E", "S", "W"].enumerated()), id: \.offset) { index, letter in
                        Text(letter)
                            .font(.custom("ReemKufi-Medium", size: 16))
                            .foregroundStyle(letter == "N" ? Color.appPrimary : Color.textSecondary)
                            .rotationEffect(.degrees(rotation - Double(index) * 90))
                            .offset(y: -radius + 44)
                            .rotationEffect(.degrees(Double(index) * 90))
                    }

                    Capsule()
                        .fill(isAligned ? Color.shareCard : Color.brandTeal)
                        .frame(width: 3, height: radius - 82)
                        .offset(y: -(radius - 82) / 2)
                        .rotationEffect(.degrees(qiblaBearing))

                    Image("home-qibla")
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                        .foregroundStyle(.white)
                        .frame(width: 42, height: 42)
                        .background { SoftArtwork(palette: .forest) }
                        .clipShape(.circle)
                        .overlay { Circle().strokeBorder(Color.softRim, lineWidth: 2) }
                        .shadow(color: .softShadow, radius: 8, y: 4)
                        .rotationEffect(.degrees(rotation - qiblaBearing))
                        .offset(y: -radius + 82)
                        .rotationEffect(.degrees(qiblaBearing))
                        .accessibilityHidden(true)
                }
                .rotationEffect(.degrees(-rotation))

                Capsule()
                    .fill(isAligned ? Color.shareCard : Color.textSecondary)
                    .frame(width: 4, height: 18)
                    .offset(y: -radius - 16)

                Circle()
                    .fill(isAligned ? Color.shareCard : Color.brandTeal)
                    .frame(width: 14, height: 14)
                    .overlay { Circle().strokeBorder(Color.softRim, lineWidth: 2) }
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
    }
}
