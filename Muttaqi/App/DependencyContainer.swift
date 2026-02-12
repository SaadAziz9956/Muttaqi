import Foundation
import SwiftData
import SwiftUI

@MainActor
final class DependencyContainer {
    let modelContainer: ModelContainer
    let userPreferences: UserPreferences
    let readingPreferences: ReadingPreferences

    private lazy var networkClient: NetworkClientProtocol = NetworkClient()
    private lazy var apiService: QuranAPIServiceProtocol = QuranAPIService(networkClient: networkClient)

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

    lazy var syncQuranDataUseCase = SyncQuranDataUseCase(
        syncRepository: syncRepo,
        readingPreferences: readingPreferences
    )
    private lazy var fetchSurahsUseCase = FetchSurahsUseCase(
        repository: quranRepo
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
        self.readingPreferences = ReadingPreferences()
    }

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
}

private struct DependencyContainerKey: EnvironmentKey {
    static let defaultValue: DependencyContainer? = nil
}

extension EnvironmentValues {
    var container: DependencyContainer? {
        get { self[DependencyContainerKey.self] }
        set { self[DependencyContainerKey.self] = newValue }
    }
}
