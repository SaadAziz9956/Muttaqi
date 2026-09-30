package com.muttaqi.android.feature.journal

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object JournalListRoute

/** The editor on an entry; a new entry has no id */
@Serializable
data class JournalEntryRoute(val entryId: String? = null)

/** The Journal screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.journalDestinations(navController: NavController) {
    composable<JournalListRoute> {
        JournalListRoute(
            onOpenEntry = { navController.navigate(JournalEntryRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
    composable<JournalEntryRoute> { entry ->
        JournalEntryRoute(
            entryId = entry.toRoute<JournalEntryRoute>().entryId,
            onBack = navController::popBackStack,
        )
    }
}
