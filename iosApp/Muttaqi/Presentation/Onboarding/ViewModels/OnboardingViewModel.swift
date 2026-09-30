import Foundation
import Shared

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
    private let quran: QuranUseCases

    init(
        userPreferences: UserPreferencesProtocol,
        notificationService: NotificationServiceProtocol,
        locationService: LocationServiceProtocol,
        quran: QuranUseCases
    ) {
        self.userPreferences = userPreferences
        self.notificationService = notificationService
        self.locationService = locationService
        self.quran = quran
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
            // Download Arabic + transliteration + English translation
            let outcome = try? await quran.sync(language: .english)
            isLoading = false
            guard let outcome, !(outcome is OutcomeFailure) else {
                setupError = QuranMessages.shared.downloadFailed(language: .english)
                return
            }
            userPreferences.setOnboardingComplete(true)
            onOnboardingComplete?()
        }
    }

    private func moveToNextStep() {
        let allSteps = OnboardingStep.allCases
        guard let currentIndex = allSteps.firstIndex(of: currentStep),
              currentIndex + 1 < allSteps.count else { return }
        currentStep = allSteps[currentIndex + 1]
    }
}
