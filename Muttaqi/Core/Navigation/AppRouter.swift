import SwiftUI

@Observable
@MainActor
final class AppRouter {
    var selectedTab: AppTab = .home
    var homePath = NavigationPath()
    var explorePath = NavigationPath()
    var quranPath = NavigationPath()
    var duaPath = NavigationPath()
    
    enum HomeDestination: Hashable {
        case placeholder // Remove this when adding real destinations
    }
    
    enum ExploreDestination: Hashable {
        case placeholder // Remove this when adding real destinations
    }
    
    enum QuranDestination: Hashable {
        case surahDetail(number: Int)
    }
    
    enum DuaDestination: Hashable {
        case placeholder // Remove this when adding real destinations
    }
    
    func pushHome(_ destination: HomeDestination) {
        homePath.append(destination)
    }
    
    func pushExplore(_ destination: ExploreDestination) {
        explorePath.append(destination)
    }
    
    func pushQuran(_ destination: QuranDestination) {
        quranPath.append(destination)
    }
    
    func pushDua(_ destination: DuaDestination) {
        duaPath.append(destination)
    }
    
    func push<D: Hashable>(_ destination: D) {
        switch selectedTab {
        case .home: homePath.append(destination)
        case .explore: explorePath.append(destination)
        case .quran: quranPath.append(destination)
        case .dua: duaPath.append(destination)
        }
    }
    
    func pop() {
        switch selectedTab {
        case .home: 
            guard !homePath.isEmpty else { return }
            homePath.removeLast()
        case .explore: 
            guard !explorePath.isEmpty else { return }
            explorePath.removeLast()
        case .quran: 
            guard !quranPath.isEmpty else { return }
            quranPath.removeLast()
        case .dua: 
            guard !duaPath.isEmpty else { return }
            duaPath.removeLast()
        }
    }
    
    func popToRoot() {
        switch selectedTab {
        case .home: homePath = NavigationPath()
        case .explore: explorePath = NavigationPath()
        case .quran: quranPath = NavigationPath()
        case .dua: duaPath = NavigationPath()
        }
    }
}
