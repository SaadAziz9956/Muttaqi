package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.quran.domain.model.Revelation
import com.muttaqi.shared.feature.quran.domain.model.Surah

/**
 * The surahs revealed where chosen that match a search, by number or by name. A number finds the surahs whose number
 * starts with it; names match without their hyphens, apostrophes and accents, so "alkahf", "Al Kahf" and "kahf" all
 * find Al-Kahf, and the Arabic matches without its harakat.
 */
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
