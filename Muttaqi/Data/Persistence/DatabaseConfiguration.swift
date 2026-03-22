import Foundation
import SwiftData

enum DatabaseConfiguration {
    static let schema = Schema([
        SurahEntity.self,
        AyahEntity.self,
        AyahTranslationEntity.self,
        TafsirEntity.self,
        ReadingProgressEntity.self
    ])

    static func makeContainer(inMemory: Bool = false) throws -> ModelContainer {
        let configuration = ModelConfiguration(
            schema: schema,
            isStoredInMemoryOnly: inMemory
        )
        return try ModelContainer(
            for: schema,
            configurations: [configuration]
        )
    }
}
