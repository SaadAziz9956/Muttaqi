import Foundation

/// Value object representing a Surah number with validation
struct SurahNumber: Hashable, Codable, Sendable, Comparable {
    let value: Int
    
    static let minimum = 1
    static let maximum = 114
    
    init?(_ value: Int) {
        guard value >= Self.minimum && value <= Self.maximum else {
            return nil
        }
        self.value = value
    }
    
    /// Unsafe initializer - crashes if invalid. Use only when you're certain the value is valid.
    init(unchecked value: Int) {
        guard value >= Self.minimum && value <= Self.maximum else {
            fatalError("Invalid surah number: \(value)")
        }
        self.value = value
    }
    
    func next() -> SurahNumber? {
        SurahNumber(value + 1)
    }
    
    func previous() -> SurahNumber? {
        SurahNumber(value - 1)
    }
    
    var isFirst: Bool { value == Self.minimum }
    var isLast: Bool { value == Self.maximum }
    
    // MARK: - Comparable
    static func < (lhs: SurahNumber, rhs: SurahNumber) -> Bool {
        lhs.value < rhs.value
    }
}
