enum AppTab: Int, CaseIterable {
    case home
    case explore
    case quran
    case dua
    
    var title: String {
        switch self {
        case .home: "Home"
        case .explore: "Explore"
        case .quran: "Quran"
        case .dua: "Dua"
        }
    }
    
    // Iconsax icons: Linear for unselected tabs, Bold for the selected one
    var icon: String {
        switch self {
        case .home: "home-linear"
        case .explore: "search-normal-linear"
        case .quran: "book-saved-linear"
        case .dua: "moon-linear"
        }
    }

    var selectedIcon: String {
        switch self {
        case .home: "home-bold"
        case .explore: "search-normal-bold"
        case .quran: "book-saved-bold"
        case .dua: "moon-bold"
        }
    }
}
