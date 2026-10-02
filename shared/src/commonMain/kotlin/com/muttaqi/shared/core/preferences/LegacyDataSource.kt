package com.muttaqi.shared.core.preferences

fun interface LegacyDataSource {
    fun text(key: String): String?
}

internal expect fun platformLegacyDataSource(): LegacyDataSource
