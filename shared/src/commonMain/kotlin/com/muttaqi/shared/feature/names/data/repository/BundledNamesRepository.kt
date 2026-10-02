package com.muttaqi.shared.feature.names.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.names.data.dto.NamesBookDto
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BundledNamesRepository(
    private val content: BundledContentSource,
    private val dispatchers: DispatcherProvider,
) : NamesRepository {

    private val mutex = Mutex()
    private var book: NamesBookDto? = null

    override suspend fun names(language: Language): List<AllahName> = load().toNames(language)

    private suspend fun load(): NamesBookDto = mutex.withLock {
        book ?: withContext(dispatchers.io) {
            ContentJson.decodeFromString<NamesBookDto>(content.read("AsmaUlHusna.json"))
        }.also { book = it }
    }
}
