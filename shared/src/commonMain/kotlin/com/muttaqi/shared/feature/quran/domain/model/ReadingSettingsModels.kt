package com.muttaqi.shared.feature.quran.domain.model

import com.muttaqi.shared.core.model.Language

/** How the reader lays out a surah */
enum class ReadingMode(val storedValue: String) {
    /** Each ayah on its own card with its transliteration and translation */
    WithTranslation("withTranslation"),
    /** The Arabic alone, flowing in pages like a printed Mushaf */
    ArabicOnly("arabicOnly");

    companion object {
        fun fromStoredValue(value: String?): ReadingMode = entries.firstOrNull { it.storedValue == value } ?: WithTranslation
    }
}

/** The reader's text size, as a percentage of the standard sizes, kept between 70% and 200% */
class FontSize(percentage: Int = DEFAULT_PERCENT) {
    val percentage: Int = percentage.coerceIn(MINIMUM_PERCENT, MAXIMUM_PERCENT)

    val arabicSize: Double get() = ARABIC_BASE * percentage / 100
    val transliterationSize: Double get() = TRANSLITERATION_BASE * percentage / 100
    val translationSize: Double get() = TRANSLATION_BASE * percentage / 100

    fun increased(): FontSize = FontSize(percentage + STEP)
    fun decreased(): FontSize = FontSize(percentage - STEP)

    override fun equals(other: Any?): Boolean = other is FontSize && other.percentage == percentage
    override fun hashCode(): Int = percentage
    override fun toString(): String = "FontSize($percentage%)"

    companion object {
        const val MINIMUM_PERCENT = 70
        const val MAXIMUM_PERCENT = 200
        const val DEFAULT_PERCENT = 100
        private const val STEP = 2

        // Sizes at 100%, in points, matching the app's standard type
        private const val ARABIC_BASE = 24.0
        private const val TRANSLITERATION_BASE = 16.0
        private const val TRANSLATION_BASE = 16.0
    }
}

/** How the reader has chosen to read */
data class ReadingSettings(
    val mode: ReadingMode = ReadingMode.WithTranslation,
    val fontSize: FontSize = FontSize(),
    val language: Language = Language.English,
)
