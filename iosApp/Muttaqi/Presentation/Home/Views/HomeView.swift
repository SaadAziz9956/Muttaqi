import Shared
import SwiftUI

struct HomeView: View {
    @State private var viewModel: HomeViewModel
    @Environment(AppRouter.self) private var router
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL

    init(viewModel: HomeViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            // Re-evaluated every minute, so the next prayer and the Hijri date (which turns at Maghrib) stay current
            TimelineView(.everyMinute) { context in
                let next = viewModel.nextPrayer(at: context.date)

                VStack(spacing: 0) {
                    HStack {
                        Text(viewModel.hijriDate(at: context.date))
                            .font(.custom("ReemKufi-Regular", size: 13))
                            .foregroundStyle(.appPrimary)
                        Spacer(minLength: 8)
                        NextPrayerPill(upcoming: next, locationState: viewModel.locationState, onSetLocation: setLocation)
                    }
                    .padding(.top, 6)

                    greeting
                        .padding(.top, 30)

                    if let today = viewModel.schedule?.today {
                        // Once Isha has passed, the next prayer is tomorrow's Fajr, so nothing in today's row is picked
                        let isToday = next.map { Calendar.current.isDate($0.date, inSameDayAs: today.date(prayer: .fajr)) } ?? false
                        PrayerTimesStrip(times: today, next: isToday ? next?.prayer : nil)
                            .padding(.top, 28)
                    }

                    HomeBento(viewModel: viewModel)
                        .padding(.top, 18)

                    VStack(spacing: 14) {
                        if let reading = viewModel.lastReading {
                            SurahShortcut(
                                surah: reading.surah,
                                title: "Continue \(reading.surah.englishName)",
                                subtitle: "Ayah \(reading.progress.lastAyahNumber)"
                            ) {
                                router.openInQuran(surah: reading.surah, ayah: reading.progress.lastAyahNumber)
                            }
                        }
                        // Reading al-Kahf on Friday is a sunnah, so on Fridays it's a tap away
                        if viewModel.isFriday(context.date), let kahf = viewModel.kahf,
                           viewModel.lastReading?.surah.number != kahf.number {
                            SurahShortcut(surah: kahf, title: "Surah \(kahf.englishName)", subtitle: "Friday") {
                                router.openInQuran(surah: kahf, ayah: 1)
                            }
                        }
                    }
                    .padding(.top, 14)

                    VStack(spacing: 18) {
                        if let ayah = viewModel.ayahOfTheDay {
                            AyahOfTheDayCard(dailyAyah: ayah) {
                                router.openInQuran(surah: ayah.surah, ayah: ayah.ayah.numberInSurah)
                            }
                        }
                        if let hadith = viewModel.hadithOfTheDay {
                            HadithOfTheDayCard(hadith: hadith)
                        }
                        if let dua = viewModel.duaOfTheDay {
                            DuaOfTheDayCard(dua: dua)
                        }
                    }
                    .padding(.top, 28)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 32)
            }
        }
        .background { SoftBackdrop() }
        // Home has no title of its own, so its empty bar is hidden and the page starts under the status bar
        .toolbar(.hidden, for: .navigationBar)
        // Soft fade under the status bar so scrolled content doesn't collide with the clock
        .overlay(alignment: .top) {
            LinearGradient(colors: [Color.softCanvas, Color.softCanvas.opacity(0)], startPoint: .top, endPoint: .bottom)
                .frame(height: 70)
                .ignoresSafeArea(edges: .top)
                .allowsHitTesting(false)
        }
        .task {
            await viewModel.refresh()
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                Task { await viewModel.refresh() }
            }
        }
    }

    private var greeting: some View {
        VStack(spacing: 0) {
            Text("Assalam - o - Alaikum")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)

            if let quote = viewModel.quote, let translation = quote.ayah.translation {
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

    private func setLocation() {
        if viewModel.locationState == .denied, let settings = URL(string: UIApplication.openSettingsURLString) {
            openURL(settings)
        } else {
            Task { await viewModel.requestLocation() }
        }
    }
}
