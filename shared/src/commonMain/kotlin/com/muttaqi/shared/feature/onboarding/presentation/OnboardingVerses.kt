package com.muttaqi.shared.feature.onboarding.presentation

data class OnboardingVerse(val arabic: String, val translation: String, val source: String?)

object OnboardingVerses {
    val basmala = OnboardingVerse(
        arabic = "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ",
        translation = "In the name of Allah,\nthe Entirely Merciful, the Especially Merciful.",
        source = null,
    )

    val lovesThePure = OnboardingVerse(
        arabic = "وَاللَّهُ يُحِبُّ الْمُطَّهِّرِينَ",
        translation = "And Allah loves those who purify themselves.",
        source = "Quran (9:108)",
    )
}
