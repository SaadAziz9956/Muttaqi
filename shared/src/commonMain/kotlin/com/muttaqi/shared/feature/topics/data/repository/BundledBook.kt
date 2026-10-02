package com.muttaqi.shared.feature.topics.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class BundledBook<T : Any>(
    private val content: BundledContentSource,
    private val dispatchers: DispatcherProvider,
    private val fileName: String,
    private val decode: (String) -> T,
) {
    private val mutex = Mutex()
    private var book: T? = null

    suspend fun get(): T = mutex.withLock {
        book ?: withContext(dispatchers.io) { decode(content.read(fileName)) }.also { book = it }
    }
}
