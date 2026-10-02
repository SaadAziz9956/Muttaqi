package com.muttaqi.android.feature.quran

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.android.feature.share.ShareRoute
import kotlinx.serialization.Serializable

@Serializable
data object QuranListRoute

@Serializable
data class SurahRoute(val surahNumber: Int, val startAyah: Int = 0)

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
