package com.muttaqi.shared.feature.dhikr.data.progress

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create

/** The progress the Swift app wrote with `UserDefaults.set(Data, forKey:)`, which it encoded as UTF-8 JSON */
class UserDefaultsLegacyDhikrProgressSource(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : LegacyDhikrProgressSource {
    @OptIn(BetaInteropApi::class)
    override fun json(key: String): String? {
        val data = defaults.dataForKey(key) ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }
}

internal actual fun platformLegacyDhikrProgressSource(): LegacyDhikrProgressSource = UserDefaultsLegacyDhikrProgressSource()
