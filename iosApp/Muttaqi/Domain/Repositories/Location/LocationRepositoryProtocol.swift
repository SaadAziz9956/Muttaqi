import Foundation

enum LocationAccess: Sendable {
    case notDetermined
    case denied
    case granted
}

protocol LocationRepositoryProtocol: Sendable {
    var access: LocationAccess { get }
    func requestAccess() async -> LocationAccess
    /// The last saved fix, available immediately and offline
    func lastKnownCoordinates() -> Coordinates?
    /// Asks for a fresh fix and saves it; nil when access is missing or no fix arrives in time
    func refreshCoordinates() async -> Coordinates?
}
