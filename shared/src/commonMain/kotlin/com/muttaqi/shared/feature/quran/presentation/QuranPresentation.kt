package com.muttaqi.shared.feature.quran.presentation

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.quran.domain.model.Ayah

/** What the Quran screens say when something goes wrong, the same words on both platforms */
object QuranMessages {
    /** Downloading the Quran or a translation failed, e.g. during onboarding's setup */
    fun downloadFailed(language: Language): String =
        "Failed to download ${language.displayName} translation. Please check your connection."

    fun surahFailed(surahNumber: Int): String =
        "Unable to load Surah $surahNumber. Please check your connection and try again."

    const val SURAH_FAILED_SUGGESTION = "Make sure you have an internet connection and the Quran data is downloaded."

    fun invalidSurah(surahNumber: Int): String =
        "Invalid Surah number: $surahNumber. Please select a valid Surah (1-114)."

    const val INVALID_SURAH_SUGGESTION = "Please navigate to a valid Surah."

    fun tafsirFailed(error: DomainError): String = when (error) {
        DomainError.NoConnection -> "The Internet connection appears to be offline."
        DomainError.NotFound -> "No tafseer was found for this surah."
        is DomainError.Unexpected -> error.message ?: "Something went wrong. Please try again."
    }
}

/** An ayah as a share card: without the end-of-ayah sign, which the card doesn't number */
fun Ayah.toSharePassage() = SharePassage(
    arabic = arabicText.replace(END_OF_AYAH, "").trim(),
    transliteration = null,
    translation = translation.orEmpty(),
    reference = "Quran ($reference)",
)

/** An ayah as plain text, as copied */
fun Ayah.copyText(): String = listOfNotNull(arabicText, translation, "Quran ($reference)").joinToString("\n\n")

/** The Arabic without the end-of-ayah sign, which the screens draw as their own ornament or medallion */
fun Ayah.arabicWithoutEndSign(): String = arabicText.replace(END_OF_AYAH, "").trim()

private const val END_OF_AYAH = "۝"
