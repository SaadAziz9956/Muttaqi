import Shared
import SwiftUI

struct MainTabView: View {
    @State private var router = AppRouter()
    /// Home's shared view model, kept here as the tabs redraw, since making it starts its loading and location
    @State private var home = SharedViewModel(HomeViewModels.shared.home()) { $0.state }

    init() {
        Self.configureTabBarAppearance()
    }

    var body: some View {
        TabView(selection: $router.selectedTab) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                tabView(for: tab)
            }
        }
        .tint(.appPrimary)
        .environment(router)
    }

    private func tabView(for tab: AppTab) -> some View {
        NavigationStack(path: pathBinding(for: tab)) {
            tabContent(for: tab)
                // Any card or page in any tab can open the Share page
                .navigationDestination(for: SharePassage.self) { passage in
                    ShareView(passage: passage)
                }
        }
        .tag(tab)
        .tabItem {
            Label {
                Text(tab.title)
            } icon: {
                Image(router.selectedTab == tab ? tab.selectedIcon : tab.icon)
                    .renderingMode(.template)
            }
        }
    }

    @ViewBuilder
    private func tabContent(for tab: AppTab) -> some View {
        switch tab {
        case .home:
            HomeView(screen: home)
                .navigationDestination(for: AppRouter.HomeDestination.self) { dest in
                    switch dest {
                    case .qibla:
                        QiblaView()
                    case .journal:
                        JournalListView()
                    case .journalEntry(let id):
                        JournalEntryView(entryId: id)
                    case .dhikrList:
                        DhikrListView()
                    case .names:
                        NamesView()
                    case .emotions:
                        EmotionsListView()
                    case .emotion(let id):
                        TopicPageView(emotionId: id)
                    case .dhikr(let id):
                        DhikrCounterView(dhikrId: id)
                    }
                }
        case .explore:
            ExploreView()
                .navigationDestination(for: AppRouter.ExploreDestination.self) { dest in
                    switch dest {
                    case .topic(let id):
                        TopicPageView(exploreTopicId: id)
                    }
                }
        case .quran:
            QuranListView()
                .navigationDestination(for: AppRouter.QuranDestination.self) { dest in
                    switch dest {
                    case .surahDetail(let surah, let startAyah):
                        SurahDetailView(surah: surah, startAyah: startAyah)
                    }
                }
        case .dua:
            DuaListView()
                .navigationDestination(for: AppRouter.DuaDestination.self) { dest in
                    switch dest {
                    case .category(let id):
                        DuaCategoryView(categoryId: id)
                    case .chapter(let id):
                        DuaChapterView(chapterId: id)
                    }
                }
        }
    }

    private func pathBinding(for tab: AppTab) -> Binding<NavigationPath> {
        switch tab {
        case .home: $router.homePath
        case .explore: $router.explorePath
        case .quran: $router.quranPath
        case .dua: $router.duaPath
        }
    }

    private static func configureTabBarAppearance() {
        let appearance = UITabBarAppearance()
        let fontAttributes: [NSAttributedString.Key: Any] = [
            .font: UIFont(name: "ReemKufi-Medium", size: 12)!
        ]
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = fontAttributes
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = fontAttributes
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
}
