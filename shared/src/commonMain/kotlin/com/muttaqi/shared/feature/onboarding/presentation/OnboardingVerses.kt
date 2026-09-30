package com.muttaqi.shared.feature.onboarding.presentation

/** A verse on an onboarding step, with its translation */
data class OnboardingVerse(val arabic: String, val translation: String, val source: String?)

/**
 * The verses on the welcome and goals steps, in English only (the reader hasn't picked a language yet), in Saheeh
 * International's words, as everywhere else in the app
 */
object OnboardingVerses {
    /** The basmala, on the welcome step */
    val basmala = OnboardingVerse(
        arabic = "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ",
        // Quran 1:1, broken after the first comma to sit on two lines
        translation = "In the name of Allah,\nthe Entirely Merciful, the Especially Merciful.",
        source = null,
    )

    /** The end of Quran 9:108, at the foot of the goals step: its last clause, with a capital as it starts the line */
    val lovesThePure = OnboardingVerse(
        arabic = "وَاللَّهُ يُحِبُّ الْمُطَّهِّرِينَ",
        translation = "And Allah loves those who purify themselves.",
        source = "Quran (9:108)",
    )
}
