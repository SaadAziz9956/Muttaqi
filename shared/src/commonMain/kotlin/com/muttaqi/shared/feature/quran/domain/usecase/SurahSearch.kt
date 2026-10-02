package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.quran.domain.model.Revelation
import com.muttaqi.shared.feature.quran.domain.model.Surah

class FilterSurahs(private val folder: TextFolder) {
    operator fun invoke(surahs: List<Surah>, query: String, revelation: Revelation?): List<Surah> {
        val byPlace = if (revelation == null) surahs else surahs.filter { it.revelationType == revelation.label }
        val words = searchKey(query)
        if (words.isEmpty()) return byPlace
        val number = words.takeIf { key -> key.all { it in '0'..'9' } }?.toIntOrNull()
        if (number != null) return byPlace.filter { it.number.toString().startsWith(number.toString()) }
        return byPlace.filter { surah ->
            listOf(surah.englishName, surah.englishNameTranslation, surah.name).any { searchKey(it).contains(words) }
        }
    }

    private fun searchKey(text: String): String = folder.fold(text).filter { it.isLetterOrDigit() }
}
