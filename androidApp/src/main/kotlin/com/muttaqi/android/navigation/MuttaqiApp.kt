package com.muttaqi.android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.oneui.LocalTabBarInset
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiTab
import com.muttaqi.android.designsystem.oneui.OneUiTabBar
import com.muttaqi.android.designsystem.oneui.OneUiTheme
import com.muttaqi.android.designsystem.oneui.TabBarInset
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
        val selected = Tab.entries.indexOfFirst { tab -> destination?.hierarchy?.any { it.hasRoute(tab.graph::class) } == true }
        val tabs = remember { Tab.entries.map { OneUiTab(it.label, it.icon, it.selectedIcon) } }

        Box(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalTabBarInset provides if (onTabRoot) TabBarInset else 0.dp) {
                NavHost(navController, startDestination = HomeTab) {
                    navigation<HomeTab>(startDestination = HomeRoute) {
                        homeDestinations(navController)
                        prayerDestinations(navController)
                        journalDestinations(navController)
                        dhikrDestinations(navController)
                        namesDestinations(navController)
                        emotionsDestinations(navController)
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
            AnimatedVisibility(
                visible = onTabRoot,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(tween(200)) + slideInVertically(tween(300, easing = OneUiDefaults.Easing)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 },
            ) {
                OneUiTheme {
                    OneUiTabBar(
                        tabs = tabs,
                        selected = selected,
                        onSelect = { index ->
                            navController.navigate(Tab.entries[index].graph) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.navigationBarsPadding(),
                    )
                }
            }
        }
    }
}
