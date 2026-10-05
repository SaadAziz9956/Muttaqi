package com.muttaqi.android.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.feature.dhikr.DhikrListRoute
import com.muttaqi.android.feature.journal.JournalEntryRoute
import com.muttaqi.android.feature.journal.JournalListRoute
import com.muttaqi.android.feature.names.NamesRoute
import com.muttaqi.android.feature.prayer.QiblaRoute
import com.muttaqi.android.feature.quran.SurahRoute
import com.muttaqi.android.feature.settings.SettingsRoute
import com.muttaqi.android.feature.share.ShareRoute
import com.muttaqi.android.feature.topics.EmotionsRoute
import com.muttaqi.android.feature.topics.TopicPageRoute
import com.muttaqi.android.navigation.ExploreTab
import com.muttaqi.android.navigation.QuranTab
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeDestinations(navController: NavController) {
    composable<HomeRoute> {
        HomeRoute(
            onOpenQibla = { navController.navigate(QiblaRoute) },
            onOpenDhikr = { navController.navigate(DhikrListRoute) },
            onOpenNames = { navController.navigate(NamesRoute) },
            onOpenJournal = { navController.navigate(JournalListRoute) },
            onOpenJournalEntry = { navController.navigate(JournalEntryRoute(it)) },
            onOpenEmotions = { navController.navigate(EmotionsRoute) },
            onOpenTopic = { navController.openInTab(ExploreTab, TopicPageRoute(TopicChips.ExploreGroup, it)) },
            onOpenSurah = { surah, ayah -> navController.openInTab(QuranTab, SurahRoute(surah, ayah)) },
            onShare = { navController.navigate(ShareRoute(it)) },
            onOpenSettings = { navController.navigate(SettingsRoute) },
        )
    }
}

internal fun NavController.openInTab(tab: Any, route: Any) {
    navigate(tab) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
    }
    navigate(route)
}
