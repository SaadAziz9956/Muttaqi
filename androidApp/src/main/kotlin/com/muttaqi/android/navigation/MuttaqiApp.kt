package com.muttaqi.android.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
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

/** The four tabs, as on iOS: Iconsax linear icons, and bold for the selected tab */
private enum class Tab(val graph: Any, val root: Any, val label: String, @DrawableRes val icon: Int, @DrawableRes val selectedIcon: Int) {
    Home(HomeTab, HomeRoute, "Home", R.drawable.ic_home_linear, R.drawable.ic_home_bold),
    Explore(ExploreTab, ExploreRoute, "Explore", R.drawable.ic_search_normal_linear, R.drawable.ic_search_normal_bold),
    Quran(QuranTab, QuranListRoute, "Quran", R.drawable.ic_book_saved_linear, R.drawable.ic_book_saved_bold),
    Dua(DuaTab, DuaListRoute, "Dua", R.drawable.ic_moon_linear, R.drawable.ic_moon_bold),
}

/**
 * The app: onboarding on first launch, then the tabs. Each tab keeps its own back stack; features add their screens
 * through their `…Destinations` functions, and screens every tab can open (share, the topic page) sit outside the tabs.
 * The bar shows only on each tab's first screen, as on iOS
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MuttaqiApp() {
    OnboardingGate {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val destination = backStack?.destination
        val onTabRoot = Tab.entries.any { tab -> destination?.hasRoute(tab.root::class) == true }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (onTabRoot) {
                    ShortNavigationBar(containerColor = MuttaqiTheme.soft.surface) {
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
                                icon = { Icon(painterResource(if (selected) tab.selectedIcon else tab.icon), null, Modifier.size(24.dp)) },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                                colors = ShortNavigationBarItemDefaults.colors(
                                    selectedIconColor = MuttaqiTheme.soft.appPrimary,
                                    selectedTextColorTopIconPosition = MuttaqiTheme.soft.appPrimary,
                                    selectedTextColorStartIconPosition = MuttaqiTheme.soft.appPrimary,
                                    selectedIndicatorColor = MuttaqiTheme.soft.tintedSurface,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
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
        }
    }
}
