package com.muttaqi.shared.feature.dua.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QuranicDuaDto(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val transliteration: String,
    val translations: Map<String, String>,
)

@Serializable
internal data class HisnBookDto(val categories: List<Category>) {
    @Serializable
    data class Category(val id: String, val title: String, val chapters: List<Chapter>)

    @Serializable
    data class Chapter(val id: Int, val title: String, val titleArabic: String, val duas: List<Entry>)

    @Serializable
    data class Entry(
        val id: Int,
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val translationUrdu: String? = null,
        val translationUrduCredit: String? = null,
        @SerialName("repeat") val repeatCount: Int,
        val reference: String,
        val source: String,
        val grade: Map<String, String>? = null,
    )
}
