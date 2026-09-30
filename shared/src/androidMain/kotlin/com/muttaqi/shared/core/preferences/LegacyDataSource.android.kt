package com.muttaqi.shared.core.preferences

// The Android app is new, so there's nothing from before the move to read
internal actual fun platformLegacyDataSource(): LegacyDataSource = LegacyDataSource { null }
