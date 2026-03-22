import Foundation

protocol QuranSyncRepositoryProtocol: Sendable {
    func syncArabicAndTransliteration() async throws
    func syncTranslation(language: String) async throws
    func syncSurahList() async throws
}
