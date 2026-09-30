import Foundation

struct GetQiblaDirectionUseCase: Sendable {
    private let repository: PrayerTimesRepositoryProtocol

    init(repository: PrayerTimesRepositoryProtocol) {
        self.repository = repository
    }

    func execute(from coordinates: Coordinates) -> QiblaDirection {
        repository.qiblaDirection(from: coordinates)
    }
}
