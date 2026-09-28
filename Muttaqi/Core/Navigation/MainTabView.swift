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
                    case .comingSoon(let title, let icon):
                        ComingSoonView(title: title, icon: icon)
                    }
                }
        case .explore: Text("Explore")
        case .quran:
            QuranListView(viewModel: container.makeQuranListViewModel())
                .navigationDestination(for: AppRouter.QuranDestination.self) { dest in
                    switch dest {
                    case .surahDetail(let surah, let startAyah):
                        SurahDetailView(coordinator: container.makeSurahDetailCoordinator(surah: surah, startAyah: startAyah))
                    }
                }
        case .dua:
            DuaListView(viewModel: container.makeDuaListViewModel())
                .navigationDestination(for: AppRouter.DuaDestination.self) { dest in
                    switch dest {
                    case .category(let category):
                        DuaCategoryView(category: category)
                    case .chapter(let chapter):
                        DuaChapterView(chapter: chapter)
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
