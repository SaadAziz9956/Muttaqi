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
        case qibla
        case journal
        /// A journal entry to read or edit; a blank one for a new entry
        case journalEntry(JournalEntry)
        case dhikrList
        case names
        case emotions
        /// One emotion's page, opened at that emotion's tab
        case emotion(id: String)
        /// One dhikr with its counter
        case dhikr(Dhikr)
    }
    
    enum ExploreDestination: Hashable {
        /// One topic's page, opened at that topic's tab
        case topic(id: String)
    }
    
    enum QuranDestination: Hashable {
        /// `startAyah` is the ayah (number within the surah) to open at, e.g. when continuing where the reader left off
        case surahDetail(surah: Surah, startAyah: Int? = nil)
    }
    
    enum DuaDestination: Hashable {
        case category(DuaCategory)
        case chapter(DuaChapter)
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

    /// Switches to the Quran tab and opens the surah at the given ayah, e.g. from the Ayah of the Day
    func openInQuran(surah: Surah, ayah: Int) {
        quranPath = NavigationPath()
        quranPath.append(QuranDestination.surahDetail(surah: surah, startAyah: ayah))
        selectedTab = .quran
    }
    
    /// Switches to the Explore tab and opens the topic, e.g. from the topic of the day on Home
    func openInExplore(topicID: String) {
        explorePath = NavigationPath()
        explorePath.append(ExploreDestination.topic(id: topicID))
        selectedTab = .explore
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
