package com.muttaqi.shared.feature.dhikr.data.progress

// The Android app is new, so there's no progress from before the move to read
internal actual fun platformLegacyDhikrProgressSource(): LegacyDhikrProgressSource = LegacyDhikrProgressSource { null }
