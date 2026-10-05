package com.muttaqi.android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.muttaqi.android.R
import com.muttaqi.android.feature.dhikr.dhikrDestinations
import com.muttaqi.android.feature.dua.DuaListRoute
import com.muttaqi.android.feature.dua.duaDestinations
import com.muttaqi.android.feature.home.HomeRoute
import com.muttaqi.android.feature.home.homeDestinations
import com.muttaqi.android.feature.journal.journalDestinations
import com.muttaqi.android.feature.names.namesDestinations
import com.muttaqi.android.feature.onboarding.OnboardingGate
import com.muttaqi.android.feature.prayer.prayerDestinations
import com.muttaqi.android.feature.quran.QuranListRoute
import com.muttaqi.android.feature.quran.quranDestinations
import com.muttaqi.android.feature.settings.settingsDestinations
import com.muttaqi.android.feature.share.shareDestinations
import com.muttaqi.android.feature.topics.ExploreRoute
import com.muttaqi.android.feature.topics.emotionsDestinations
import com.muttaqi.android.feature.topics.exploreDestinations
import com.muttaqi.android.feature.topics.topicPageDestinations
import kotlinx.serialization.Serializable

@Serializable data object HomeTab
@Serializable data object ExploreTab
@Serializable data object QuranTab
@Serializable data object DuaTab

private enum class Tab(val graph: Any, val root: Any, val label: String, @DrawableRes val icon: Int, @DrawableRes val selectedIcon: Int) {
    Home(HomeTab, HomeRoute, "Home", R.drawable.ic_home, R.drawable.ic_home_filled),
    Explore(ExploreTab, ExploreRoute, "Explore", R.drawable.ic_explore, R.drawable.ic_explore_filled),
    Quran(QuranTab, QuranListRoute, "Quran", R.drawable.ic_menu_book, R.drawable.ic_menu_book_filled),
    Dua(DuaTab, DuaListRoute, "Dua", R.drawable.ic_dark_mode, R.drawable.ic_dark_mode_filled),
}

@Composable
fun MuttaqiApp() {
    OnboardingGate {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val destination = backStack?.destination
        val onTabRoot = Tab.entries.any { tab -> destination?.hasRoute(tab.root::class) == true }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (onTabRoot) {
                    ShortNavigationBar {
                        Tab.entries.forEach { tab ->
                            val selected = destination?.hierarchy?.any { it.hasRoute(tab.graph::class) } == true
                            ShortNavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.graph) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(painterResource(if (selected) tab.selectedIcon else tab.icon), contentDescription = null) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).consumeWindowInsets(padding)) {
                NavHost(navController, startDestination = HomeTab) {
                    navigation<HomeTab>(startDestination = HomeRoute) {
                        homeDestinations(navController)
                        prayerDestinations(navController)
                        journalDestinations(navController)
                        dhikrDestinations(navController)
                        namesDestinations(navController)
                        emotionsDestinations(navController)
                        settingsDestinations(navController)
                    }
                    navigation<ExploreTab>(startDestination = ExploreRoute) {
                        exploreDestinations(navController)
                    }
                    navigation<QuranTab>(startDestination = QuranListRoute) {
                        quranDestinations(navController)
                    }
                    navigation<DuaTab>(startDestination = DuaListRoute) {
                        duaDestinations(navController)
                    }
                    topicPageDestinations(navController)
                    shareDestinations(navController)
                }
            }
        }
    }
}
