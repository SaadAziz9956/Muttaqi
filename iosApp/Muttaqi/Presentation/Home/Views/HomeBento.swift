import Shared
import SwiftUI

struct HomeBento: View {
    let screen: SharedViewModel<HomeViewModel, HomeState>

    private let spacing: CGFloat = 14
    private let tileHeight: CGFloat = 132

    private var state: HomeState { screen.state }

    var body: some View {
        VStack(spacing: spacing) {
            HStack(spacing: spacing) {
                qibla
                    .frame(height: tileHeight * 2 + spacing)
                VStack(spacing: spacing) {
                    dhikr.frame(height: tileHeight)
                    name.frame(height: tileHeight)
                }
            }

            journal
                .frame(minHeight: 96)

            HStack(spacing: spacing) {
                emotions.frame(height: tileHeight)
                explore.frame(height: tileHeight)
            }
        }
    }

    private var qibla: some View {
        tile(artwork: .forest, label: "Qibla", icon: "home-qibla", tint: .white) {
            dispatch(HomeIntentQiblaTapped.shared)
        } content: {
            VStack(alignment: .leading, spacing: 0) {
                Spacer(minLength: 0)
                QiblaPointer(viewModel: screen.viewModel, bearing: state.qibla?.bearing)
                .frame(width: 112, height: 112)
                .frame(maxWidth: .infinity)
                Spacer(minLength: 0)
                if let qibla = state.qibla {
                    Text("\(Int(qibla.bearing.rounded()))°")
                        .font(.custom("ReemKufi-Medium", size: 30, relativeTo: .title))
                    Text("Makkah · \(qibla.distanceInKilometers.formatted(.number.precision(.fractionLength(0)))) km")
                        .font(.custom("ReemKufi-Regular", size: 12, relativeTo: .caption))
                        .opacity(0.8)
                } else {
                    Text("Find the Qibla")
                        .font(.custom("ReemKufi-Medium", size: 18, relativeTo: .headline))
                }
            }
        }
        .accessibilityLabel(state.qibla.map { "Qibla, \(Int($0.bearing.rounded())) degrees" } ?? "Qibla")
    }

    private var dhikr: some View {
        tile(label: "Dikr", icon: "repeat-circle-linear") {
            dispatch(HomeIntentDhikrTapped.shared)
        } content: {
            VStack(alignment: .leading, spacing: 2) {
                Spacer(minLength: 0)
                if state.dhikrToday > 0 {
                    Text(Int(state.dhikrToday), format: .number)
                        .font(.custom("ReemKufi-Medium", size: 30, relativeTo: .title))
                        .foregroundStyle(.appPrimary)
                        .contentTransition(.numericText())
                    Text("said today")
                        .font(.custom("ReemKufi-Regular", size: 12, relativeTo: .caption))
                        .foregroundStyle(.textSecondary)
                } else {
                    Text("Begin today's dhikr")
                        .font(.custom("ReemKufi-Medium", size: 16, relativeTo: .headline))
                        .foregroundStyle(.appPrimary)
                }
            }
        }
    }

    private var name: some View {
        tile(artwork: .dawn, label: "Name of the day", icon: nil) {
            dispatch(HomeIntentNameTapped.shared)
        } content: {
            if let name = state.nameOfTheDay {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer(minLength: 0)
                    Text(AttributedString.arabic(name.arabic, size: 26))
                        .foregroundStyle(.appPrimary)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text(name.transliteration)
                        .font(.custom("ReemKufi-Medium", size: 15, relativeTo: .headline))
                        .foregroundStyle(.appPrimary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
    }

    private var journal: some View {
        let entry = state.journalToday
        return Button {
            dispatch(HomeIntentJournalTapped.shared)
        } label: {
            HStack(spacing: 14) {
                VStack(alignment: .leading, spacing: 8) {
                    tileLabel("Journal", icon: "book-linear", tint: .textSecondary)
                    Text(entry?.preview ?? "What are you grateful for today?")
                        .font(.custom("ReemKufi-Medium", size: 17, relativeTo: .headline))
                        .foregroundStyle(.appPrimary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }
                Spacer(minLength: 0)
                Button {
                    dispatch(HomeIntentTodaysEntryTapped.shared)
                } label: {
                    SoftCircle(size: 44, filled: true) {
                        Image(entry == nil ? "add-linear" : "arrow-right-01-linear")
                            .resizable()
                            .frame(width: 20, height: 20)
                    }
                }
                .buttonStyle(SoftPressStyle())
                .accessibilityLabel(entry == nil ? "New entry" : "Today's entry")
                .accessibilityHint(entry == nil ? "Starts today's entry" : "Opens today's entry")
            }
            .padding(18)
            .frame(maxWidth: .infinity, alignment: .leading)
            .softCard(glass: true)
        }
        .buttonStyle(SoftPressStyle())
        .accessibilityHint("Opens your journal")
    }

    private var emotions: some View {
        tile(artwork: .lagoon, label: "Emotions", icon: "happyemoji-linear") {
            dispatch(HomeIntentEmotionsTapped.shared)
        } content: {
            VStack(alignment: .leading) {
                Spacer(minLength: 0)
                Text("How do you feel?")
                    .font(.custom("ReemKufi-Medium", size: 17, relativeTo: .headline))
                    .foregroundStyle(.appPrimary)
            }
        }
    }

    private var explore: some View {
        tile(label: "Topic of the day", icon: nil) {
            dispatch(HomeIntentTopicTapped.shared)
        } content: {
            if let topic = state.topicOfTheDay {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer(minLength: 0)
                    Image(topic.icon)
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.brandTeal)
                        .padding(9)
                        .background(.tintedSurface, in: .circle)
                    Spacer(minLength: 0)
                    Text(topic.title)
                        .font(.custom("ReemKufi-Medium", size: 17, relativeTo: .headline))
                        .foregroundStyle(.appPrimary)
                        .lineLimit(2)
                        .minimumScaleFactor(0.85)
                }
            }
        }
    }

    private func dispatch(_ intent: HomeIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private func tile<Content: View>(
        artwork: SoftArtwork.Palette? = nil,
        label: String,
        icon: String?,
        tint: Color = .textSecondary,
        action: @escaping () -> Void,
        @ViewBuilder content: () -> Content
    ) -> some View {
        let content = content()
        return Button(action: action) {
            VStack(alignment: .leading, spacing: 0) {
                HStack(alignment: .top) {
                    tileLabel(label, icon: icon, tint: artwork == .forest ? .white.opacity(0.85) : tint)
                    Spacer(minLength: 4)
                    SoftCircle(size: 30) {
                        Image("arrow-right-01-linear")
                            .resizable()
                            .frame(width: 14, height: 14)
                            .rotationEffect(.degrees(-45))
                    }
                }
                content
            }
            .foregroundStyle(artwork == .forest ? Color.white : Color.appPrimary)
            .padding(16)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .softCard(rim: 3, glass: artwork == nil, artwork: artwork)
        }
        .buttonStyle(SoftPressStyle())
    }

    private func tileLabel(_ text: String, icon: String?, tint: Color) -> some View {
        HStack(spacing: 6) {
            if let icon {
                Image(icon)
                    .resizable()
                    .renderingMode(.template)
                    .scaledToFit()
                    .frame(width: 15, height: 15)
            }
            Text(text)
                .font(.custom("ReemKufi-Regular", size: 13, relativeTo: .subheadline))
                .lineLimit(1)
        }
        .foregroundStyle(tint)
    }
}

struct PrayerTimesStrip: View {
    let times: DailyPrayerTimes
    let next: Prayer?

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Prayer.allCases, id: \.self) { prayer in
                let isNext = prayer == next
                VStack(spacing: 3) {
                    Text(prayer.displayName)
                        .font(.custom("ReemKufi-Regular", size: 12, relativeTo: .caption))
                        .foregroundStyle(isNext ? Color.white.opacity(0.85) : Color.textSecondary)
                    Text(Date(times.time(prayer: prayer)), format: .dateTime.hour(.defaultDigits(amPM: .omitted)).minute())
                        .font(.custom("ReemKufi-Medium", size: 15, relativeTo: .subheadline))
                        .foregroundStyle(isNext ? Color.white : Color.appPrimary)
                        .monospacedDigit()
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background {
                    if isNext {
                        Capsule().fill(Color.shareCard).shadow(color: .softShadow, radius: 8, y: 4)
                    }
                }
                .accessibilityElement(children: .combine)
                .accessibilityAddTraits(isNext ? .isSelected : [])
            }
        }
        .padding(6)
        .softCard(cornerRadius: 30)
    }
}

struct SurahShortcut: View {
    let surah: Surah
    let title: String
    let subtitle: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Text(surah.number, format: .number)
                    .font(.custom("ReemKufi-Medium", size: 17, relativeTo: .headline))
                    .foregroundStyle(.white)
                    .frame(width: 50, height: 50)
                    .background { SoftArtwork(palette: .forest) }
                    .clipShape(.circle)
                    .overlay { Circle().strokeBorder(Color.softRim, lineWidth: 2) }
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.custom("ReemKufi-Medium", size: 16, relativeTo: .headline))
                        .foregroundStyle(.appPrimary)
                    Text(subtitle)
                        .font(.custom("ReemKufi-Regular", size: 12, relativeTo: .caption))
                        .foregroundStyle(.textSecondary)
                }
                Spacer(minLength: 8)
                SoftCircle(size: 40, filled: true) {
                    Image("arrow-right-01-linear")
                        .resizable()
                        .frame(width: 18, height: 18)
                }
            }
            .padding(10)
            .padding(.trailing, 4)
            .softCard(cornerRadius: 36, glass: true)
        }
        .buttonStyle(SoftPressStyle())
    }
}

private struct QiblaPointer: View {
    let viewModel: HomeViewModel
    let bearing: Double?
    @State private var arrow: Double?

    var body: some View {
        ZStack {
            Circle()
                .strokeBorder(.white.opacity(0.35), lineWidth: 1.5)
            Circle()
                .fill(.white.opacity(0.12))
                .padding(10)
            Image("send-2-bold")
                .resizable()
                .frame(width: 42, height: 42)
                .rotationEffect(.degrees((arrow ?? bearing ?? 0) - 45))
                .animation(.smooth(duration: 0.25), value: arrow)
                .shadow(color: .black.opacity(0.2), radius: 6, y: 3)
        }
        .task {
            for await next in viewModel.qiblaArrow {
                arrow = next?.doubleValue
            }
        }
    }
}
