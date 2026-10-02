package com.muttaqi.shared.feature.names.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode

class NamesSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Pair<AllahName, List<String>>>,
) {
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

class BuildNamesSearchIndex(private val folder: TextFolder) {
    operator fun invoke(names: List<AllahName>): NamesSearchIndex =
        NamesSearchIndex(folder, names.map { it to listOf(folder.foldName(it.transliteration), folder.foldName(it.meaning)) })
}

internal fun TextFolder.foldName(text: String): String {
    var folded = fold(text).filter { it.isLetter() }
    for ((long, short) in listOf("aa" to "a", "ee" to "i", "ii" to "i", "oo" to "u", "uu" to "u")) {
        folded = folded.replace(long, short)
    }
    return folded
}
