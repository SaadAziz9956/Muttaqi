package com.muttaqi.shared.core.text

import java.text.Normalizer

internal actual fun String.decomposedCanonically(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
