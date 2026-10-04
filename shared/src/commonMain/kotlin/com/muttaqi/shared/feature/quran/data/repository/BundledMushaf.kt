package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.concurrent.Volatile

internal data class MushafAyah(val words: String, val mark: String, val page: Int, val juz: Int)

internal fun interface MushafSource {
    suspend fun ayah(surah: Int, ayah: Int): MushafAyah?
}

internal class BundledMushaf(
    private val content: BundledContentSource,
    private val dispatchers: DispatcherProvider,
) : MushafSource {
    private val mutex = Mutex()

    @Volatile
    private var ayahs: Map<Long, MushafAyah>? = null

    override suspend fun ayah(surah: Int, ayah: Int): MushafAyah? = load()[key(surah, ayah)]

    private suspend fun load(): Map<Long, MushafAyah> = ayahs ?: mutex.withLock {
        ayahs ?: withContext(dispatchers.io) {
            ContentJson.decodeFromString<List<MushafAyahDto>>(content.read(FILE)).associate { key(it.surah, it.ayah) to it.toMushafAyah() }
        }.also { ayahs = it }
    }

    private fun key(surah: Int, ayah: Int): Long = surah * 1000L + ayah

    companion object {
        const val FILE = "hafsData_v2-0.json"
    }
}

@Serializable
internal class MushafAyahDto(
    @SerialName("sura_no") val surah: Int,
    @SerialName("aya_no") val ayah: Int,
    @SerialName("aya_text") val text: String,
    val page: Int,
    @SerialName("jozz") val juz: Int,
) {
    fun toMushafAyah(): MushafAyah = MushafAyah(
        words = text.dropLast(1).trimEnd(' ', ' '),
        mark = text.takeLast(1),
        page = page,
        juz = juz,
    )
}
