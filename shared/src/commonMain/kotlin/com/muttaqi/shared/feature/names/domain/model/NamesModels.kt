package com.muttaqi.shared.feature.names.domain.model

data class AllahName(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val meaning: String,
)

enum class NameSearchMode {
    ByNumber,
    ByName,
}
