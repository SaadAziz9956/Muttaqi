package com.muttaqi.shared.feature.names.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.names.domain.model.AllahName

fun interface NamesRepository {
    suspend fun names(language: Language): List<AllahName>
}
