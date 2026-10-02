package com.muttaqi.shared.core.model

enum class Language(val code: String, val displayName: String) {
    English("en", "English"),
    Urdu("ur", "Urdu"),
    Hindi("hi", "Hindi");

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: English
    }
}

fun Map<String, String>.inLanguage(language: Language): String = this[language.code] ?: this[Language.English.code] ?: ""
