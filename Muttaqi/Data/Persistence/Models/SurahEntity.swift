import Foundation
import SwiftData

@Model
final class SurahEntity {
    @Attribute(.unique) var number: Int
    var name: String
    var englishName: String
    var englishNameTranslation: String
    var revelationType: String
    var numberOfAyahs: Int

    @Relationship(deleteRule: .cascade, inverse: \AyahEntity.surah)
    var ayahs: [AyahEntity]

    init(
        number: Int,
        name: String,
        englishName: String,
        englishNameTranslation: String,
        revelationType: String,
        numberOfAyahs: Int
    ) {
        self.number = number
        self.name = name
        self.englishName = englishName
        self.englishNameTranslation = englishNameTranslation
        self.revelationType = revelationType
        self.numberOfAyahs = numberOfAyahs
        self.ayahs = []
    }
}
