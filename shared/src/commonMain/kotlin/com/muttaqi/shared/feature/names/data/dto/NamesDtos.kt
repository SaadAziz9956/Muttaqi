package com.muttaqi.shared.feature.names.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class NamesBookDto(val names: List<Entry>) {
    @Serializable
    data class Entry(val number: Int, val arabic: String, val transliteration: String, val meaning: Map<String, String>)
}
