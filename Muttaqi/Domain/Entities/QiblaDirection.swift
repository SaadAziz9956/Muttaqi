import Foundation

struct QiblaDirection: Equatable, Sendable {
    /// Bearing to the Kaaba in degrees clockwise from true north
    let bearing: Double
    let distanceInKilometers: Double
}
