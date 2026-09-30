package com.muttaqi.shared.feature.names.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode

/** The names folded for search once, so each keystroke only compares */
class NamesSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Pair<AllahName, List<String>>>,
) {
    /**
     * By number: the name with that number. By name: names whose transliteration or meaning contains the query,
     * ignoring case, accents, hyphens and doubled vowels, so "rahman" finds "Ar-Raḥmān" and "raheem" finds "Ar-Raḥīm"
     */
    fun search(query: String, mode: NameSearchMode): List<AllahName> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return when (mode) {
            NameSearchMode.ByNumber -> {
                val number = trimmed.toIntOrNull() ?: return emptyList()
                entries.map { it.first }.filter { it.number == number }
            }
            NameSearchMode.ByName -> {
                val folded = folder.foldName(trimmed)
                if (folded.isEmpty()) return emptyList()
                entries.filter { (_, texts) -> texts.any { folded in it } }.map { it.first }
            }
        }
    }
}

/** Builds the search index for the names in the reader's language */
class BuildNamesSearchIndex(private val folder: TextFolder) {
    operator fun invoke(names: List<AllahName>): NamesSearchIndex =
        NamesSearchIndex(folder, names.map { it to listOf(folder.foldName(it.transliteration), folder.foldName(it.meaning)) })
}

/**
 * The names' own folding on top of the shared one: letters only, so hyphens and spaces don't matter, and doubled
 * vowels collapsed, since transliterations spell long vowels either way (Rahmaan, Raheem, Ghafoor)
 */
internal fun TextFolder.foldName(text: String): String {
    var folded = fold(text).filter { it.isLetter() }
    for ((long, short) in listOf("aa" to "a", "ee" to "i", "ii" to "i", "oo" to "u", "uu" to "u")) {
        folded = folded.replace(long, short)
    }
    return folded
}
