package com.muttaqi.shared.core.preferences

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create

class UserDefaultsLegacyDataSource(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : LegacyDataSource {
    @OptIn(BetaInteropApi::class)
    override fun text(key: String): String? {
        val data = defaults.dataForKey(key) ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }
}

internal actual fun platformLegacyDataSource(): LegacyDataSource = UserDefaultsLegacyDataSource()
