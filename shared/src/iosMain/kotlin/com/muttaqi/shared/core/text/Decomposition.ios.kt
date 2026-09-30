package com.muttaqi.shared.core.text

import platform.Foundation.NSString
import platform.Foundation.decomposedStringWithCanonicalMapping

@Suppress("CAST_NEVER_SUCCEEDS")
internal actual fun String.decomposedCanonically(): String =
    (this as NSString).decomposedStringWithCanonicalMapping
