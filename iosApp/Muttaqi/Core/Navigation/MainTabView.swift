import SwiftUI

struct MainTabView: View {
    @State private var router = AppRouter()
    @Environment(\.container) private var _container

    private var container: DependencyContainer {
        guard let _container else {
            fatalError("DependencyContainer not set in environment — inject via .environment(\\.container, container)")
        }
        return _container
    }

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
                    ShareView(passage: passage, language: container.readingPreferences.getSelectedLanguage().code)
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
            HomeView(viewModel: container.makeHomeViewModel())
                .navigationDestination(for: AppRouter.HomeDestination.self) { dest in
                    switch dest {
                    case .qibla:
                        QiblaView(viewModel: container.makeQiblaViewModel())
                    case .journal:
                        JournalListView(viewModel: container.makeJournalListViewModel())
                    case .journalEntry(let entry):
                        JournalEntryView(viewModel: container.makeJournalEntryViewModel(entry: entry))
                    case .dhikrList:
                        DhikrListView(viewModel: container.makeDhikrListViewModel())
                    case .names:
                        NamesView(viewModel: container.makeNamesViewModel())
                    case .emotions:
                        EmotionsListView(viewModel: container.makeEmotionsViewModel())
                    case .emotion(let id):
                        TopicPageView(title: "Emotions", viewModel: container.makeEmotionPageViewModel(selectedID: id))
                    case .dhikr(let dhikr):
                        DhikrCounterView(viewModel: container.makeDhikrCounterViewModel(dhikr: dhikr))
                    }
                }
        case .explore:
            ExploreView(viewModel: container.makeExploreViewModel())
                .navigationDestination(for: AppRouter.ExploreDestination.self) { dest in
                    switch dest {
                    case .topic(let id):
                        TopicPageView(title: "Explore", viewModel: container.makeExploreTopicViewModel(selectedID: id))
                    }
                }
        case .quran:
            QuranListView(viewModel: container.makeQuranListViewModel())
                .navigationDestination(for: AppRouter.QuranDestination.self) { dest in
                    switch dest {
                    case .surahDetail(let surah, let startAyah):
                        SurahDetailView(coordinator: container.makeSurahDetailCoordinator(surah: surah, startAyah: startAyah))
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
