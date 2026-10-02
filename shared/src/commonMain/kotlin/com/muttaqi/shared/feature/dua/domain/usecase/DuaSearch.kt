package com.muttaqi.shared.feature.dua.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.DuaChapter

data class DuaSearchResult(val category: DuaCategory, val chapter: DuaChapter)

class DuaSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Pair<DuaSearchResult, String>>,
) {
    fun search(query: String): List<DuaSearchResult> {
        val words = folder.fold(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        return entries.filter { (_, text) -> words.all { it in text } }.map { it.first }
    }
}

class BuildDuaSearchIndex(private val folder: TextFolder) {
    operator fun invoke(categories: List<DuaCategory>): DuaSearchIndex = DuaSearchIndex(
        folder,
        categories.flatMap { category ->
            category.chapters.map { chapter ->
                val parts = listOf(category.title, chapter.title, chapter.titleArabic.orEmpty()) +
                    chapter.entries.flatMap { listOf(it.translation, it.transliteration, it.arabic, it.source) }
                DuaSearchResult(category, chapter) to folder.fold(parts.joinToString(" "))
            }
        },
    )
}
