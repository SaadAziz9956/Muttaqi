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
    
    var icon: String {
        switch self {
        case .home: "ic_home"
        case .explore: "ic_explore"
        case .quran: "ic_book"
        case .dua: "ic_duaa"
        }
    }
    
    var selectedIcon: String {
        switch self {
        case .home: "ic_home_selected"
        case .explore: "ic_explore"
        case .quran: "ic_book"
        case .dua: "ic_duaa"
        }
    }
}
