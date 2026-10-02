import Shared
import SwiftUI

struct HomeView: View {
    let screen: SharedViewModel<HomeViewModel, HomeState>
    @Environment(AppRouter.self) private var router
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL

    private var state: HomeState { screen.state }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                HStack {
                    Text(state.hijriDate)
                        .font(.custom("ReemKufi-Regular", size: 13))
                        .foregroundStyle(.appPrimary)
                    Spacer(minLength: 8)
                    NextPrayerPill(upcoming: state.nextPrayer, asksForLocation: state.asksForLocation) {
                        dispatch(HomeIntentSetLocationTapped.shared)
                    }
                }
                .padding(.top, 6)

                greeting
                    .padding(.top, 30)

                if let today = state.schedule?.today {
                    PrayerTimesStrip(times: today, next: state.nextPrayerToday)
                        .padding(.top, 28)
                }

                HomeBento(screen: screen)
                    .padding(.top, 18)

                VStack(spacing: 14) {
                    if let reading = state.lastReading {
                        SurahShortcut(
                            surah: reading.surah,
                            title: "Continue \(reading.surah.englishName)",
                            subtitle: "Ayah \(reading.ayahNumber)"
                        ) {
                            dispatch(HomeIntentContinueReadingTapped.shared)
                        }
                    }
                    if let kahf = state.fridayKahf {
                        SurahShortcut(surah: kahf, title: "Surah \(kahf.englishName)", subtitle: "Friday") {
                            dispatch(HomeIntentKahfTapped.shared)
                        }
                    }
                }
                .padding(.top, 14)

                VStack(spacing: 18) {
                    if let ayah = state.ayahOfTheDay {
                        AyahOfTheDayCard(dailyAyah: ayah, dispatch: dispatch)
                    }
                    if let hadith = state.hadithOfTheDay {
                        HadithOfTheDayCard(hadith: hadith, dispatch: dispatch)
                    }
                    if let dua = state.duaOfTheDay {
                        DuaOfTheDayCard(dua: dua, dispatch: dispatch)
                    }
                }
                .padding(.top, 28)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
        .toolbar(.hidden, for: .navigationBar)
        .overlay(alignment: .top) {
            LinearGradient(colors: [Color.softCanvas, Color.softCanvas.opacity(0)], startPoint: .top, endPoint: .bottom)
                .frame(height: 70)
                .ignoresSafeArea(edges: .top)
                .allowsHitTesting(false)
        }
        .task {
            dispatch(HomeIntentRefresh.shared)
            for await effect in screen.viewModel.effects {
                handle(effect)
            }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                dispatch(HomeIntentRefresh.shared)
            }
        }
    }

    private var greeting: some View {
        VStack(spacing: 0) {
            Text("Assalam - o - Alaikum")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)

            if let quote = state.greeting, let translation = quote.ayah.translation {
                let style = TranslationStyle(for: translation, size: 14)
                Text(translation.quoted)
                    .font(style.font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 20)

                Text("Quran (\(quote.reference))")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
            }
        }
    }

    private func dispatch(_ intent: HomeIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private func handle(_ effect: HomeEffect) {
        switch onEnum(of: effect) {
        case .openQibla: router.pushHome(.qibla)
        case .openDhikr: router.pushHome(.dhikrList)
        case .openNames: router.pushHome(.names)
        case .openJournal: router.pushHome(.journal)
        case .openJournalEntry(let entry): router.pushHome(.journalEntry(id: entry.entryId))
        case .openEmotions: router.pushHome(.emotions)
        case .openTopic(let topic): router.openInExplore(topicID: topic.topicId)
        case .openSurah(let open): router.openInQuran(surah: open.surah, ayah: Int(open.ayahNumber))
        case .openShare(let share): router.push(share.passage)
        case .copy(let copy): UIPasteboard.general.string = copy.text
        case .openSettings:
            if let settings = URL(string: UIApplication.openSettingsURLString) { openURL(settings) }
        }
    }
}
