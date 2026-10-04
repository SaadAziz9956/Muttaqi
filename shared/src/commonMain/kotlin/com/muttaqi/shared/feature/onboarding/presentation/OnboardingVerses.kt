package com.muttaqi.shared.feature.onboarding.presentation

import com.muttaqi.shared.feature.quran.presentation.MushafSamples

data class OnboardingVerse(val arabic: String, val translation: String, val source: String?)

object OnboardingVerses {
    val basmala = OnboardingVerse(
        arabic = MushafSamples.BISMILLAH,
        translation = "In the name of Allah,\nthe Entirely Merciful, the Especially Merciful.",
        source = null,
    )

    val lovesThePure = OnboardingVerse(
        arabic = MushafSamples.LOVES_THE_PURE,
        translation = "And Allah loves those who purify themselves.",
        source = "Quran (9:108)",
    )
}
