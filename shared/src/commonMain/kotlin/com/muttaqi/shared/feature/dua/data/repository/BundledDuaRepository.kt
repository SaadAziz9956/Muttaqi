package com.muttaqi.shared.feature.dua.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.data.dto.HisnBookDto
import com.muttaqi.shared.feature.dua.data.dto.QuranicDuaDto
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.dua.domain.repository.DuaCategoryRepository
import com.muttaqi.shared.feature.dua.domain.repository.QuranicDuaRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BundledDuaRepository(
    private val content: BundledContentSource,
    private val dispatchers: DispatcherProvider,
) : DuaCategoryRepository, QuranicDuaRepository {

    private val mutex = Mutex()
    private var quranic: List<QuranicDuaDto>? = null
    private var hisn: HisnBookDto? = null

    override suspend fun categories(language: Language): List<DuaCategory> {
        val (quranic, hisn) = load()
        return listOf(quranic.toRabbanaCategory(language)) + hisn.toCategories(language)
    }

    override suspend fun quranicDuas(language: Language): List<QuranicDua> = load().first.map { it.toQuranicDua(language) }

    private suspend fun load(): Pair<List<QuranicDuaDto>, HisnBookDto> = mutex.withLock {
        val loadedQuranic = quranic ?: withContext(dispatchers.io) {
            ContentJson.decodeFromString<List<QuranicDuaDto>>(content.read("Duas.json"))
        }.also { quranic = it }
        val loadedHisn = hisn ?: withContext(dispatchers.io) {
            ContentJson.decodeFromString<HisnBookDto>(content.read("HisnAlMuslim.json"))
        }.also { hisn = it }
        loadedQuranic to loadedHisn
    }
}
