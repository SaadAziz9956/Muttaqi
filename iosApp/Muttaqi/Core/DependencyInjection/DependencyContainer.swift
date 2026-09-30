import Foundation
import Shared
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
    private lazy var tafsirAPIService: TafsirAPIServiceProtocol = TafsirAPIService(networkClient: networkClient)
    private lazy var locationService: LocationServiceProtocol = LocationService()

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
    private lazy var tafsirRepo: TafsirRepositoryProtocol = TafsirRepository(
        modelContainer: modelContainer,
        apiService: tafsirAPIService
    )
    // Karachi method with Hanafi Asr by default
    private lazy var prayerTimesRepo: PrayerTimesRepositoryProtocol = AdhanPrayerTimesRepository()
    private lazy var duaRepo: DuaRepositoryProtocol = BundledDuaRepository()
    private lazy var locationRepo: LocationRepositoryProtocol = LocationRepository(
        service: locationService,
        preferences: userPreferences
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
    private lazy var fetchTafsirUseCase = FetchTafsirUseCase(
        repository: tafsirRepo
    )
    private lazy var updateReadingProgressUseCase = UpdateReadingProgressUseCase(
        repository: readingProgressRepo
    )
    private lazy var fetchAyahUseCase = FetchAyahUseCase(
        repository: quranRepo,
        languagePreferences: readingPreferences
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
        // The journal now lives in the shared database; entries written before are brought over from SwiftData once
        let modelContainer = self.modelContainer
        Task { await SwiftDataJournalImport.run(from: modelContainer) }
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

    func makeHomeViewModel() -> HomeViewModel {
        HomeViewModel(
            location: locationRepo,
            getPrayerSchedule: GetPrayerScheduleUseCase(repository: prayerTimesRepo),
            fetchAyah: fetchAyahUseCase,
            getAyahOfTheDay: GetAyahOfTheDayUseCase(fetchAyah: fetchAyahUseCase),
            getDuaOfTheDay: GetDuaOfTheDayUseCase(repository: duaRepo, languagePreferences: readingPreferences),
            getLastReading: getLastReadingUseCase,
            fetchSurahs: fetchSurahsUseCase,
            getQiblaDirection: GetQiblaDirectionUseCase(repository: prayerTimesRepo),
            compass: CompassService(),
            journal: SharedViewModel(JournalViewModels.shared.today()) { $0.state }
        )
    }

    func makeQiblaViewModel() -> QiblaViewModel {
        QiblaViewModel(
            location: locationRepo,
            compass: CompassService(),
            getQiblaDirection: GetQiblaDirectionUseCase(repository: prayerTimesRepo)
        )
    }

    func makeQuranListViewModel() -> QuranListViewModel {
        QuranListViewModel(
            fetchSurahsUseCase: fetchSurahsUseCase,
            getLastReadingUseCase: getLastReadingUseCase,
            languagePreferences: readingPreferences
        )
    }
    
    func makeSurahDetailCoordinator(surah: Surah, startAyah: Int? = nil) -> SurahDetailCoordinator {
        guard let surahNumber = SurahNumber(surah.number) else {
            fatalError("Invalid surah number: \(surah.number)")
        }

        return SurahDetailCoordinator(
            initialSurah: surahNumber,
            headerSurah: surah,
            startAyah: startAyah,
            fetchSurahs: fetchSurahsUseCase,
            fetchAyahs: fetchAyahsUseCase,
            syncQuranData: syncQuranDataUseCase,
            readingPreferences: readingPreferences,
            fetchTafsir: fetchTafsirUseCase,
            updateReadingProgress: updateReadingProgressUseCase
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
