package com.muttaqi.shared.core.preferences

/**
 * Values the Swift app saved as data (`UserDefaults.set(Data, forKey:)`) rather than as text, which settings can't
 * read: Dikr's progress and the last known coordinates, each JSON that Swift's JSONEncoder wrote. Only iOS has any; a
 * feature reads them until its next save writes the same JSON as text
 */
fun interface LegacyDataSource {
    /** The data saved under [key] as UTF-8 text, or null when there's none */
    fun text(key: String): String?
}

/** This platform's [LegacyDataSource]: iOS reads the standard user defaults, Android has nothing to read */
internal expect fun platformLegacyDataSource(): LegacyDataSource
