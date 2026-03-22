import Foundation

struct FontSize: Equatable, Codable, Sendable {
    private(set) var value: Int
    
    static let minimum = 10
    static let maximum = 32
    static let `default` = FontSize(16)
    
    init(_ value: Int) {
        self.value = min(Self.maximum, max(Self.minimum, value))
    }
    
    mutating func increase(by step: Int = 2) {
        value = min(Self.maximum, value + step)
    }
    
    mutating func decrease(by step: Int = 2) {
        value = max(Self.minimum, value - step)
    }
    
    func increased(by step: Int = 2) -> FontSize {
        var copy = self
        copy.increase(by: step)
        return copy
    }
    
    func decreased(by step: Int = 2) -> FontSize {
        var copy = self
        copy.decrease(by: step)
        return copy
    }
}
