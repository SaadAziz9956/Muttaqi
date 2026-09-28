import SwiftUI

struct QiblaView: View {
    @State private var viewModel: QiblaViewModel
    @Environment(\.openURL) private var openURL

    init(viewModel: QiblaViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        Group {
            switch viewModel.state {
            case .locating:
                ProgressView()
                    .tint(.appPrimary)
            case .needsLocation(let access):
                locationNeeded(access)
            case .ready:
                if let qibla = viewModel.qibla {
                    compass(qibla)
                }
            }
        }
        .navigationTitle("Qibla")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .tabBar)
        .task { await viewModel.locate() }
        .task(id: viewModel.qibla != nil) {
            if viewModel.qibla != nil { await viewModel.trackHeading() }
        }
        .sensoryFeedback(.success, trigger: viewModel.isAligned) { _, aligned in aligned }
    }

    private func compass(_ qibla: QiblaDirection) -> some View {
        VStack(spacing: 0) {
            Spacer()

            QiblaCompassDial(
                qiblaBearing: qibla.bearing,
                rotation: viewModel.dialRotation,
                isAligned: viewModel.isAligned
            )
            .frame(width: 300, height: 300)
            .animation(.easeOut(duration: 0.25), value: viewModel.dialRotation)

            VStack(spacing: 8) {
                Text(instruction(for: qibla))
                    .font(.custom("ReemKufi-Regular", size: 22))
                    .foregroundStyle(viewModel.isAligned ? .appPrimary : .textPrimary)
                    .contentTransition(.numericText())

                Text("\(Int(qibla.bearing.rounded()))° from North · \(qibla.distanceInKilometers.formatted(.number.precision(.fractionLength(0)))) km to Makkah")
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)

                if !viewModel.isCompassAvailable {
                    Text("This device has no compass. Use one to face \(Int(qibla.bearing.rounded()))° from North.")
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                        .multilineTextAlignment(.center)
                } else if viewModel.needsCalibration {
                    Text("Move your phone in a figure-eight to calibrate the compass.")
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(.top, 40)
            .padding(.horizontal, 32)

            Spacer()
            Spacer()
        }
        .accessibilityElement(children: .combine)
    }

    private func instruction(for qibla: QiblaDirection) -> String {
        guard let turn = viewModel.turnAngle else {
            return "Face \(Int(qibla.bearing.rounded()))° from North"
        }
        if viewModel.isAligned { return "You're facing the Qibla" }
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
                if access == .denied, let settings = URL(string: UIApplication.openSettingsURLString) {
                    openURL(settings)
                } else {
                    Task { await viewModel.requestLocation() }
                }
            }
            .buttonStyle(.borderedProminent)
            .tint(.appPrimary)
        }
    }
}

/// Compass card that turns with the phone so north stays north; the Kaaba marker sits at the Qibla bearing,
/// and the phone is facing the Qibla when the marker reaches the pointer at the top
private struct QiblaCompassDial: View {
    let qiblaBearing: Double
    let rotation: Double
    let isAligned: Bool

    var body: some View {
        GeometryReader { geometry in
            let radius = min(geometry.size.width, geometry.size.height) / 2

            ZStack {
                ZStack {
                    Circle().fill(.tintedSurface)
                    Circle().strokeBorder(Color.brandTeal.opacity(0.5), lineWidth: 1)

                    ForEach(0..<72, id: \.self) { tick in
                        let isMajor = tick % 6 == 0
                        Capsule()
                            .fill(isMajor ? Color.textPrimary : Color.textSecondary.opacity(0.6))
                            .frame(width: isMajor ? 2 : 1, height: isMajor ? 12 : 6)
                            .offset(y: -radius + 14)
                            .rotationEffect(.degrees(Double(tick) * 5))
                    }

                    ForEach(Array(["N", "E", "S", "W"].enumerated()), id: \.offset) { index, letter in
                        Text(letter)
                            .font(.custom("ReemKufi-Medium", size: 16))
                            .foregroundStyle(letter == "N" ? Color.appPrimary : Color.textSecondary)
                            // Counter-rotated so the letters stay upright as the dial turns
                            .rotationEffect(.degrees(rotation - Double(index) * 90))
                            .offset(y: -radius + 40)
                            .rotationEffect(.degrees(Double(index) * 90))
                    }

                    Capsule()
                        .fill(Color.brandTeal)
                        .frame(width: 3, height: radius - 70)
                        .offset(y: -(radius - 70) / 2)
                        .rotationEffect(.degrees(qiblaBearing))

                    Text("🕋")
                        .font(.system(size: 30))
                        .rotationEffect(.degrees(rotation - qiblaBearing))
                        .offset(y: -radius + 72)
                        .rotationEffect(.degrees(qiblaBearing))
                        .accessibilityHidden(true)
                }
                .rotationEffect(.degrees(-rotation))

                // Fixed pointer: the direction the phone is facing
                Image(systemName: "arrowtriangle.down.fill")
                    .font(.system(size: 18))
                    .foregroundStyle(isAligned ? Color.appPrimary : Color.textSecondary)
                    .offset(y: -radius - 14)

                Circle()
                    .fill(isAligned ? Color.appPrimary : Color.brandTeal)
                    .frame(width: 12, height: 12)
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
    }
}
