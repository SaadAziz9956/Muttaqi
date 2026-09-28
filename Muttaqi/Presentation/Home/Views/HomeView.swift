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
                VStack(spacing: 0) {
                    VStack(alignment: .trailing, spacing: 7) {
                        NextPrayerPill(
                            upcoming: viewModel.nextPrayer(at: context.date),
                            locationState: viewModel.locationState,
                            onSetLocation: setLocation
                        )

                        Text(viewModel.hijriDate(at: context.date))
                            .font(.custom("ReemKufi-Regular", size: 12))
                            .foregroundStyle(.appPrimary)
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .padding(.top, 6)

                    greeting
                        .padding(.top, 34)

                    QuickActionsRow { action in
                        router.pushHome(action == .qibla ? .qibla : .comingSoon(title: action.title, icon: action.icon))
                    }
                    .padding(.top, 40)

                    if let ayah = viewModel.ayahOfTheDay {
                        AyahOfTheDayCard(dailyAyah: ayah) {
                            router.openInQuran(surah: ayah.surah, ayah: ayah.ayah.numberInSurah)
                        }
                        .padding(.top, 40)
                    }

                    if let dua = viewModel.duaOfTheDay {
                        DuaOfTheDayCard(dua: dua)
                            .padding(.top, 24)
                    }
                }
                .padding(.horizontal, 22)
                .padding(.bottom, 32)
            }
        }
        // Home has no title of its own, so its empty bar is hidden and the page starts under the status bar
        .toolbar(.hidden, for: .navigationBar)
        // Soft fade under the status bar so scrolled content doesn't collide with the clock
        .overlay(alignment: .top) {
            LinearGradient(
                colors: [Color(.systemBackground), Color(.systemBackground).opacity(0)],
                startPoint: .top,
                endPoint: .bottom
            )
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
                Text("\u{201C}\(translation)\u{201D}")
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
