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
    private lazy var locationService: LocationServiceProtocol = LocationService()

    // MARK: - Repositories
    // Karachi method with Hanafi Asr by default
    private lazy var prayerTimesRepo: PrayerTimesRepositoryProtocol = AdhanPrayerTimesRepository()
    private lazy var duaRepo: DuaRepositoryProtocol = BundledDuaRepository()
    private lazy var locationRepo: LocationRepositoryProtocol = LocationRepository(
        service: locationService,
        preferences: userPreferences
    )

    // MARK: - Use Cases
    // The Quran is shared code now; this reads one ayah from it for Home's verses
    private lazy var fetchAyahUseCase = FetchAyahUseCase()

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
            quran: QuranUseCases.shared
        )
    }

    func makeHomeViewModel() -> HomeViewModel {
        HomeViewModel(
            location: locationRepo,
            getPrayerSchedule: GetPrayerScheduleUseCase(repository: prayerTimesRepo),
            fetchAyah: fetchAyahUseCase,
            getAyahOfTheDay: GetAyahOfTheDayUseCase(fetchAyah: fetchAyahUseCase),
            getDuaOfTheDay: GetDuaOfTheDayUseCase(repository: duaRepo, languagePreferences: readingPreferences),
            quran: QuranUseCases.shared,
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

    // MARK: - Quran

    /// Readies the shared Quran at launch, during the splash: the reading progress SwiftData kept is brought over
    /// once, and for someone past onboarding the Quran text, which the Swift code also kept in SwiftData, is
    /// downloaded into the shared database the first time after the update. The splash waits for that a little, so
    /// Home opens with its ayahs; a slow or failed download carries on or retries from the Quran tab.
    func prepareQuran() async {
        await StoredReadingProgressImport(modelContainer: modelContainer).run()
        guard userPreferences.isOnboardingComplete() else { return }
        let sync = Task { _ = try? await QuranUseCases.shared.syncIfNeeded() }
        await withTaskGroup(of: Void.self) { group in
            group.addTask { await sync.value }
            group.addTask { try? await Task.sleep(for: .seconds(8)) }
            await group.next()
            group.cancelAll()
        }
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
