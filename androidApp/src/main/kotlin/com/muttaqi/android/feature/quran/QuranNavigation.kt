package com.muttaqi.android.feature.quran

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.android.feature.share.ShareRoute
import kotlinx.serialization.Serializable

@Serializable
data object QuranListRoute

/** A surah to read; [startAyah] is the ayah (number within the surah) to open at, 0 for the start */
@Serializable
data class SurahRoute(val surahNumber: Int, val startAyah: Int = 0)

/** The Quran tab: the surahs, and a surah to read with its settings and explanation */
fun NavGraphBuilder.quranDestinations(navController: NavController) {
    composable<QuranListRoute> {
        QuranListRoute(onOpenSurah = { surah, ayah -> navController.navigate(SurahRoute(surah, ayah)) })
    }
    composable<SurahRoute> { entry ->
        val route = entry.toRoute<SurahRoute>()
        SurahReaderRoute(
            surahNumber = route.surahNumber,
            startAyah = route.startAyah,
            onShare = { navController.navigate(ShareRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
}
