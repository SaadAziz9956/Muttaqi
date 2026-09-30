package com.muttaqi.shared.feature.dua.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Duas.json: Quranic duas, each the dua portion of an ayah, cut from the app's Quran editions */
@Serializable
internal data class QuranicDuaDto(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val transliteration: String,
    val translations: Map<String, String>,
)

/** HisnAlMuslim.json: Hisn al-Muslim by Sa'id al-Qahtani, grouped into categories */
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
        /** The published Urdu, where there is one, and whose it is */
        val translationUrdu: String? = null,
        val translationUrduCredit: String? = null,
        @SerialName("repeat") val repeatCount: Int,
        val reference: String,
        val source: String,
    )
}
