package com.muttaqi.shared.core.domain

import kotlin.concurrent.Volatile

internal class RecentCache<K : Any, V : Any>(private val capacity: Int) {
    @Volatile
    private var entries: Map<K, V> = emptyMap()

    operator fun get(key: K): V? = entries[key]

    operator fun set(key: K, value: V) {
        val updated = entries - key + (key to value)
        entries = if (updated.size > capacity) updated - updated.keys.first() else updated
    }

    fun clear() {
        entries = emptyMap()
    }
}
