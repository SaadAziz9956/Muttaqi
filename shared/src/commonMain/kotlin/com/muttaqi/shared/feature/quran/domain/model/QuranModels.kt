package com.muttaqi.shared.feature.quran.domain.model

/** One of the Quran's 114 surahs, as listed */
data class Surah(
    val number: Int,
    /** The Arabic name, e.g. «سورة البقرة» */
    val name: String,
    /** Transliterated, e.g. "Al-Baqara" */
    val englishName: String,
    /** Its meaning, e.g. "The Cow" */
    val englishNameTranslation: String,
    /** "Meccan" or "Medinan", as the Quran API gives it */
    val revelationType: String,
    val numberOfAyahs: Int,
) {
    val revelation: Revelation? get() = Revelation.entries.firstOrNull { it.label == revelationType }

    companion object {
        const val FIRST = 1
        const val LAST = 114
    }
}

/** Where a surah was revealed */
enum class Revelation(val label: String) {
    Meccan("Meccan"),
    Medinan("Medinan"),
}

/** One ayah in the reader's translation language */
data class Ayah(
    /** Its number in the whole Quran, 1 to 6,236; also what identifies it on screen */
    val number: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    /** Uthmani script, as the Quran API gives it */
    val arabicText: String,
    val transliteration: String?,
    /** Null when the language's translation isn't stored */
    val translation: String?,
    val juz: Int,
    /** Its page in the Madinah Mushaf */
    val page: Int,
    val hizbQuarter: Int,
) {
    /** e.g. "2:255" */
    val reference: String get() = "$surahNumber:$numberInSurah"
}

/** An ayah together with its surah, so it can be shown with its reference and opened in the reader */
data class DailyAyah(val surah: Surah, val ayah: Ayah) {
    val reference: String get() = "${surah.number}:${ayah.numberInSurah}"
}

/** A surah laid out to read: its ayahs, and the surahs either side of it */
data class SurahReading(
    val surah: Surah,
    val ayahs: List<Ayah>,
    val previousSurah: Surah?,
    val nextSurah: Surah?,
) {
    /** The ayahs shown one by one: Al-Fatiha's first ayah is its Bismillah, which is shown above them instead */
    val displayAyahs: List<Ayah> = if (surah.number == 1) ayahs.drop(1) else ayahs

    /** Every surah opens with the Bismillah except At-Tawbah */
    val showsBismillah: Boolean get() = surah.number != 9

    val bismillahText: String
        get() = ayahs.firstOrNull()?.takeIf { surah.number == 1 }?.arabicText ?: BISMILLAH

    /** Al-Fatiha's own translated first ayah; other surahs show the English, since the Bismillah isn't an ayah of theirs */
    val bismillahTranslation: String
        get() = ayahs.firstOrNull()?.takeIf { surah.number == 1 }?.translation ?: BISMILLAH_TRANSLATION

    /** The ayahs split into the Madinah Mushaf's pages, for Arabic Only reading */
    val pages: List<MushafPage> = displayAyahs.fold(mutableListOf<MushafPage>()) { pages, ayah ->
        val last = pages.lastOrNull()
        if (last?.number == ayah.page) {
            pages[pages.lastIndex] = last.copy(ayahs = last.ayahs + ayah)
        } else {
            pages += MushafPage(id = ayah.number, number = ayah.page, ayahs = listOf(ayah))
        }
        pages
    }

    private companion object {
        const val BISMILLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        const val BISMILLAH_TRANSLATION = "In the Name of Allah—the Most Compassionate, Most Merciful."
    }
}

/** One page of the Madinah Mushaf within a surah */
data class MushafPage(
    /** The page's first ayah number, so pages and ayah cards share one set of on-screen ids for reading progress */
    val id: Int,
    val number: Int,
    val ayahs: List<Ayah>,
)
