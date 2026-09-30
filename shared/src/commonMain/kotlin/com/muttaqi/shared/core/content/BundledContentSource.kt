package com.muttaqi.shared.core.content

import kotlinx.serialization.json.Json

/**
 * The texts bundled with both apps, from content/data: Android reads them from its assets, iOS from the app bundle.
 * Synchronous file I/O, so repositories call it off the main thread.
 */
fun interface BundledContentSource {
    /** The file's text, e.g. `read("HisnAlMuslim.json")` */
    fun read(fileName: String): String
}

/** How bundled JSON is decoded: fields the app doesn't use are ignored, so the content can grow without breaking it */
val ContentJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = false
}
