package com.muttaqi.android.feature.dua

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.android.feature.share.ShareRoute
import kotlinx.serialization.Serializable

@Serializable
data object DuaListRoute

@Serializable
data class DuaCategoryRoute(val categoryId: String)

@Serializable
data class DuaChapterRoute(val chapterId: String)

fun NavGraphBuilder.duaDestinations(navController: NavController) {
    composable<DuaListRoute> {
        DuaListRoute(
            onOpenCategory = { navController.navigate(DuaCategoryRoute(it)) },
            onOpenChapter = { navController.navigate(DuaChapterRoute(it)) },
        )
    }
    composable<DuaCategoryRoute> { entry ->
        DuaCategoryRoute(
            categoryId = entry.toRoute<DuaCategoryRoute>().categoryId,
            onOpenChapter = { navController.navigate(DuaChapterRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
    composable<DuaChapterRoute> { entry ->
        DuaChapterRoute(
            chapterId = entry.toRoute<DuaChapterRoute>().chapterId,
            onShare = { navController.navigate(ShareRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
}
