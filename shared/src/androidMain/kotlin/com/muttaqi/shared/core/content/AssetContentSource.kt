package com.muttaqi.shared.core.content

import android.content.Context

class AssetContentSource(private val context: Context) : BundledContentSource {
    override fun read(fileName: String): String =
        context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
