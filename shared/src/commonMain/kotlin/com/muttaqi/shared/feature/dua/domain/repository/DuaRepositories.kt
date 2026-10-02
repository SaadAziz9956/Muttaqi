package com.muttaqi.shared.feature.dua.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua

fun interface DuaCategoryRepository {
    suspend fun categories(language: Language): List<DuaCategory>
}

fun interface QuranicDuaRepository {
    suspend fun quranicDuas(language: Language): List<QuranicDua>
}
