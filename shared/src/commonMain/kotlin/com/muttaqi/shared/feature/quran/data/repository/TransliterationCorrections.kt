package com.muttaqi.shared.feature.quran.data.repository

internal object TransliterationCorrections {
    private class Correction(val received: String, val published: String)

    private val corrections = mapOf(
        (50 to 19) to Correction(
            received = "Wa jaaa'at kullu nafsim ma'ahaa saaa'iqunw wa shaheed",
            published = "Wajaat sakratu almawti bialhaqqi thalika ma kunta minhu taheedu",
        ),
    )

    fun correct(surahNumber: Int, numberInSurah: Int, transliteration: String?): String? =
        corrections[surahNumber to numberInSurah]?.takeIf { it.received == transliteration }?.published ?: transliteration
}
