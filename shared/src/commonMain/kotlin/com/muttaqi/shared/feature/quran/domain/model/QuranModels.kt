package com.muttaqi.shared.feature.quran.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
) {
    val revelation: Revelation? get() = Revelation.entries.firstOrNull { it.label == revelationType }

    companion object {
        const val FIRST = 1
        const val LAST = 114
    }
}

enum class Revelation(val label: String) {
    Meccan("Meccan"),
    Medinan("Medinan"),
}

data class Ayah(
    val number: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
    val ayahMark: String = "",
) {
    val reference: String get() = "$surahNumber:$numberInSurah"
}

data class DailyAyah(val surah: Surah, val ayah: Ayah) {
    val reference: String get() = "${surah.number}:${ayah.numberInSurah}"
}

data class SurahReading(
    val surah: Surah,
    val ayahs: List<Ayah>,
    val previousSurah: Surah?,
    val nextSurah: Surah?,
    val bismillah: Ayah?,
) {
    val showsBismillah: Boolean get() = surah.number != 1 && surah.number != 9

    val bismillahText: String get() = bismillah?.arabicText.orEmpty()

    val bismillahTranslation: String? get() = bismillah?.translation

    val pages: List<MushafPage> = ayahs.fold(mutableListOf<MushafPage>()) { pages, ayah ->
        val last = pages.lastOrNull()
        if (last?.number == ayah.page) {
            pages[pages.lastIndex] = last.copy(ayahs = last.ayahs + ayah)
        } else {
            pages += MushafPage(id = ayah.number, number = ayah.page, ayahs = listOf(ayah))
        }
        pages
    }
}

data class MushafPage(
    val id: Int,
    val number: Int,
    val ayahs: List<Ayah>,
)
