import Foundation
import SwiftData
import SwiftUI

@MainActor
final class DependencyContainer {
    let modelContainer: ModelContainer
    let userPreferences: UserPreferences
    let readingPreferences: ReadingPreferencesStore

    // MARK: - Services
    private lazy var networkClient: NetworkClientProtocol = NetworkClient()
    private lazy var apiService: QuranAPIServiceProtocol = QuranAPIService(networkClient: networkClient)

    // MARK: - Repositories
    private lazy var syncRepo: QuranSyncRepositoryProtocol = QuranSyncRepository(
        modelContainer: modelContainer,
        apiService: apiService
    )
    private lazy var quranRepo: QuranRepositoryProtocol = QuranRepository(
        modelContainer: modelContainer
    )
    private lazy var readingProgressRepo: ReadingProgressRepositoryProtocol = ReadingProgressRepository(
        modelContainer: modelContainer
    )

    // MARK: - Use Cases
    private lazy var syncQuranDataUseCase = SyncQuranDataUseCase(
        syncRepository: syncRepo,
        preferences: readingPreferences
    )
    private lazy var fetchSurahsUseCase = FetchSurahsUseCase(
        repository: quranRepo
    )
    private lazy var fetchAyahsUseCase = FetchAyahsUseCase(
        repository: quranRepo,
        languagePreferences: readingPreferences
    )
    private lazy var getLastReadingUseCase = GetLastReadingUseCase(
        progressRepository: readingProgressRepo,
        quranRepository: quranRepo
    )

    init() {
        do {
            self.modelContainer = try DatabaseConfiguration.makeContainer()
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
        self.userPreferences = UserPreferences()
        self.readingPreferences = ReadingPreferencesStore(
            storage: UserDefaultsStorage()
        )
    }

    // MARK: - Factories
    func makeOnboardingViewModel() -> OnboardingViewModel {
        OnboardingViewModel(
            userPreferences: userPreferences,
            notificationService: NotificationService(),
            locationService: LocationService(),
            syncQuranDataUseCase: syncQuranDataUseCase
        )
    }

    func makeQuranListViewModel() -> QuranListViewModel {
        QuranListViewModel(
            fetchSurahsUseCase: fetchSurahsUseCase,
            getLastReadingUseCase: getLastReadingUseCase
        )
    }
    
    func makeSurahDetailCoordinator(surahNumber: Int) -> SurahDetailCoordinator {
        guard let surah = SurahNumber(surahNumber) else {
            fatalError("Invalid surah number: \(surahNumber)")
        }
        
        return SurahDetailCoordinator(
            initialSurah: surah,
            fetchSurahs: fetchSurahsUseCase,
            fetchAyahs: fetchAyahsUseCase,
            syncQuranData: syncQuranDataUseCase,
            readingPreferences: readingPreferences
        )
    }
}

// MARK: - Environment Key
private struct DependencyContainerKey: EnvironmentKey {
    static let defaultValue: DependencyContainer? = nil
}

extension EnvironmentValues {
    var container: DependencyContainer? {
        get { self[DependencyContainerKey.self] }
        set { self[DependencyContainerKey.self] = newValue }
    }
}
