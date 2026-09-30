package com.muttaqi.shared.feature.names.domain.model

/** One of the Beautiful Names of Allah (al-Asma' al-Husna) */
data class AllahName(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    /** Short meaning, in a published translation in the reader's language */
    val meaning: String,
)

/** How the search on the 99 Names page reads the query */
enum class NameSearchMode {
    /** The name with that number, e.g. "63" */
    ByNumber,
    /** Names whose transliteration or meaning contains the query */
    ByName,
}
