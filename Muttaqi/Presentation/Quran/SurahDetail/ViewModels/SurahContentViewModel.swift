import Foundation

/// Responsible ONLY for loading and presenting surah data
@Observable
@MainActor
final class SurahContentViewModel {
    enum State: Equatable {
        case idle
        case loading
        case loaded(content: SurahContent)
        case error(SurahDetailError)
    }
    
    struct SurahContent: Equatable {
        let surah: Surah
        let ayahs: [Ayah]
        
        var displayAyahs: [Ayah] {
            // Surah 1 (Al-Fatihah) includes Bismillah as first ayah
            if surah.number == 1 {
                return Array(ayahs.dropFirst())
            }
            return ayahs
        }
        
        var showBismillah: Bool {
            // All surahs except Surah 9 (At-Tawbah) show Bismillah
            surah.number != 9
        }
        
        var bismillahText: String {
            if surah.number == 1, let first = ayahs.first {
                return first.arabicText
            }
            return "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        }
        
        var bismillahTranslation: String {
            if surah.number == 1, let first = ayahs.first {
                return first.translation ?? "In the Name of Allah—the Most Compassionate, Most Merciful."
            }
            return "In the Name of Allah—the Most Compassionate, Most Merciful."
        }
    }
    
    private(set) var state: State = .idle
    
    private let fetchSurahs: FetchSurahsUseCase
    private let fetchAyahs: FetchAyahsUseCase
    
    init(
        fetchSurahs: FetchSurahsUseCase,
        fetchAyahs: FetchAyahsUseCase
    ) {
        self.fetchSurahs = fetchSurahs
        self.fetchAyahs = fetchAyahs
    }
    
    func loadSurah(_ surahNumber: SurahNumber) async {
        state = .loading
        
        do {
            // Fetch surah metadata
            let allSurahs = try await fetchSurahs.execute()
            guard let surah = allSurahs.first(where: { $0.number == surahNumber.value }) else {
                state = .error(.invalidSurahNumber(surahNumber.value))
                return
            }
            
            // Fetch ayahs
            let ayahs = try await fetchAyahs.execute(surahNumber: surahNumber)
            
            let content = SurahContent(surah: surah, ayahs: ayahs)
            state = .loaded(content: content)
            
        } catch let error as SurahDetailError {
            state = .error(error)
        } catch {
            state = .error(.failedToLoadSurah(
                surahNumber: surahNumber.value,
                underlyingError: error.localizedDescription
            ))
        }
    }
    
    func retry(surahNumber: SurahNumber) async {
        await loadSurah(surahNumber)
    }
}
