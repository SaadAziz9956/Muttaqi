package com.muttaqi.shared.feature.onboarding.presentation

/** A verse on an onboarding step, with its translation */
data class OnboardingVerse(val arabic: String, val translation: String, val source: String?)

/** The verses on the welcome and goals steps, word for word as the iOS app has always shown them, in English only */
object OnboardingVerses {
    /** The basmala, on the welcome step */
    val basmala = OnboardingVerse(
        arabic = "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ",
        translation = "In the name of Allah,\nthe most gracious, the most merciful",
        source = null,
    )

    /** The end of Quran 9:108, at the foot of the goals step */
    val lovesThePure = OnboardingVerse(
        arabic = "وَاللَّهُ يُحِبُّ الْمُطَّهِّرِينَ",
        translation = "Allah loves those who keep themselves pure.",
        source = "Quran (9:108)",
    )
}
