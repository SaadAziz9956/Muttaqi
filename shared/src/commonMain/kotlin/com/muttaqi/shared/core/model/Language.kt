package com.muttaqi.shared.core.model

/**
 * A translation language the reader can pick. Content comes in English and Urdu; a text without a published Urdu
 * translation falls back to its English. Hindi is kept only because the Quran reader offers its translation, and the
 * codes match what the iOS app has always stored.
 */
enum class Language(val code: String, val displayName: String) {
    English("en", "English"),
    Urdu("ur", "Urdu"),
    Hindi("hi", "Hindi");

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: English
    }
}

/** A text in each language it's published in, keyed by language code; falls back to English */
fun Map<String, String>.inLanguage(language: Language): String = this[language.code] ?: this[Language.English.code] ?: ""
