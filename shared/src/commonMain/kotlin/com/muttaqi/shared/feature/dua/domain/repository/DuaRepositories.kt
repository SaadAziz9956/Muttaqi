package com.muttaqi.shared.feature.dua.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua

/** Every dua category: the Quranic duas first, then Hisn al-Muslim's */
fun interface DuaCategoryRepository {
    suspend fun categories(language: Language): List<DuaCategory>
}

/** The Quranic duas (Rabbana and Rabbi), e.g. for the Dua of the Day */
fun interface QuranicDuaRepository {
    suspend fun quranicDuas(language: Language): List<QuranicDua>
}
