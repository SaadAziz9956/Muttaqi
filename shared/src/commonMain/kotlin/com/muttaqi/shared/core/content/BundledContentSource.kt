package com.muttaqi.shared.core.content

import kotlinx.serialization.json.Json

fun interface BundledContentSource {
    fun read(fileName: String): String
}

val ContentJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = false
}
