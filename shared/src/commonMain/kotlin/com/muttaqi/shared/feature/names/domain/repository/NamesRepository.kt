package com.muttaqi.shared.feature.names.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.names.domain.model.AllahName

/** The 99 names in order, with meanings in the reader's language */
fun interface NamesRepository {
    suspend fun names(language: Language): List<AllahName>
}
