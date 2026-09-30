import Foundation

struct FontSize: Equatable, Codable, Sendable {
    private(set) var percentage: Int

    static let minimumPercent = 70
    static let maximumPercent = 200
    static let `default` = FontSize(100)

    // Base sizes at 100% — matches the static typography defaults
    static let arabicBase: CGFloat = 24
    static let transliterationBase: CGFloat = 16
    static let translationBase: CGFloat = 16

    init(_ percentage: Int) {
        self.percentage = min(Self.maximumPercent, max(Self.minimumPercent, percentage))
    }

    var arabicSize: CGFloat { Self.arabicBase * CGFloat(percentage) / 100 }
    var transliterationSize: CGFloat { Self.transliterationBase * CGFloat(percentage) / 100 }
    var translationSize: CGFloat { Self.translationBase * CGFloat(percentage) / 100 }

    func increased() -> FontSize { FontSize(percentage + 2) }
    func decreased() -> FontSize { FontSize(percentage - 2) }
}
