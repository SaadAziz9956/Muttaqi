package com.muttaqi.shared.feature.quran.domain.model

import com.muttaqi.shared.core.model.Language

enum class ReadingMode(val storedValue: String) {
    WithTranslation("withTranslation"),
    ArabicOnly("arabicOnly");

    companion object {
        fun fromStoredValue(value: String?): ReadingMode = entries.firstOrNull { it.storedValue == value } ?: WithTranslation
    }
}

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

        private const val ARABIC_BASE = 24.0
        private const val TRANSLITERATION_BASE = 16.0
        private const val TRANSLATION_BASE = 16.0
    }
}

data class ReadingSettings(
    val mode: ReadingMode = ReadingMode.WithTranslation,
    val fontSize: FontSize = FontSize(),
    val language: Language = Language.English,
)
