import SwiftUI

struct MainTabView: View {
    @State private var router = AppRouter()
    @Environment(\.container) private var container

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
        case .home: Text("Home")
        case .explore: Text("Explore")
        case .quran: QuranListView(viewModel: container!.makeQuranListViewModel())
        case .dua: Text("Dua")
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
        UITabBar.appearance().unselectedItemTintColor = UIColor(named: "UnSelectedTabTint")
    }
}
