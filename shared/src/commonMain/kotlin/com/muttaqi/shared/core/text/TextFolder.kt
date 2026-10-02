package com.muttaqi.shared.core.text

fun interface TextFolder {
    fun fold(text: String): String
}

object SearchTextFolder : TextFolder {
    override fun fold(text: String): String {
        val decomposed = text.lowercase().decomposedCanonically()
        return buildString(decomposed.length) {
            for (char in decomposed) {
                val code = char.code
                when {
                    code in 0x0300..0x036F -> Unit
                    code in 0x064B..0x065F || code == 0x0670 -> Unit
                    code in 0x06D6..0x06ED || code == 0x0640 -> Unit
                    code == 0x0622 || code == 0x0623 || code == 0x0625 || code == 0x0671 -> append('ا')
                    code == 0x0629 -> append('ه')
                    code == 0x0649 -> append('ي')
                    code in 0xFF01..0xFF5E -> append((code - 0xFEE0).toChar())
                    else -> append(char)
                }
            }
        }
    }
}

internal expect fun String.decomposedCanonically(): String
