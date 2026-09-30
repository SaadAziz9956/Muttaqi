import Foundation

enum NavigationDirection {
    case forward
    case backward
}

/// Responsible ONLY for navigation between surahs
@Observable
@MainActor
final class SurahNavigator {
    private(set) var currentSurah: SurahNumber
    private(set) var navigationDirection: NavigationDirection = .forward
    
    var canGoNext: Bool { !currentSurah.isLast }
    var canGoPrevious: Bool { !currentSurah.isFirst }
    
    init(surah: SurahNumber) {
        self.currentSurah = surah
    }
    
    func navigateNext() {
        guard let next = currentSurah.next() else { return }
        navigationDirection = .forward
        currentSurah = next
    }
    
    func navigatePrevious() {
        guard let previous = currentSurah.previous() else { return }
        navigationDirection = .backward
        currentSurah = previous
    }
}
