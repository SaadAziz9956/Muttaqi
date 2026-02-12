import Foundation

@Observable
@MainActor
final class OnboardingViewModel {
    enum Intent {
        case begin
        case saveName(String)
        case next
        case requestNotification
        case skipNotification
        case requestLocation
        case skipLocation
        case finishSetup
    }

    private(set) var currentStep: OnboardingStep = .welcome
    private(set) var isLoading = false
    private(set) var setupError: String?
    private(set) var userName: String = ""
    var onOnboardingComplete: (() -> Void)?

    private let userPreferences: UserPreferencesProtocol
    private let notificationService: NotificationServiceProtocol
    private let locationService: LocationServiceProtocol
    private let syncQuranDataUseCase: SyncQuranDataUseCase

    init(
        userPreferences: UserPreferencesProtocol,
        notificationService: NotificationServiceProtocol,
        locationService: LocationServiceProtocol,
        syncQuranDataUseCase: SyncQuranDataUseCase
    ) {
        self.userPreferences = userPreferences
        self.notificationService = notificationService
        self.locationService = locationService
        self.syncQuranDataUseCase = syncQuranDataUseCase
    }

    func send(_ intent: Intent) {
        switch intent {
        case .begin:
            currentStep = .name
        case .saveName(let name):
            userName = name
            userPreferences.setUserName(name)
            currentStep = .goals
        case .next:
            moveToNextStep()
        case .requestNotification:
            Task {
                _ = await notificationService.requestPermission()
                currentStep = .location
            }
        case .skipNotification:
            currentStep = .location
        case .requestLocation:
            Task {
                _ = await locationService.requestPermission()
                currentStep = .setup
            }
        case .skipLocation:
            currentStep = .setup
        case .finishSetup:
            performSetup()
        }
    }

    private func performSetup() {
        guard !isLoading else { return }
        isLoading = true
        setupError = nil

        Task {
            do {
                // Download Arabic + transliteration + English translation
                // TODO: Replace "en" with user-selected language when language step is added
                try await syncQuranDataUseCase.execute(language: "en")

                isLoading = false
                userPreferences.setOnboardingComplete(true)
                onOnboardingComplete?()
            } catch {
                isLoading = false
                setupError = error.localizedDescription
            }
        }
    }

    private func moveToNextStep() {
        let allSteps = OnboardingStep.allCases
        guard let currentIndex = allSteps.firstIndex(of: currentStep),
              currentIndex + 1 < allSteps.count else { return }
        currentStep = allSteps[currentIndex + 1]
    }
}
