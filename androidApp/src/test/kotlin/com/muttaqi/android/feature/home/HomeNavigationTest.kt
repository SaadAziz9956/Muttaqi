package com.muttaqi.android.feature.home

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muttaqi.android.feature.quran.QuranListRoute
import com.muttaqi.android.feature.quran.SurahRoute
import com.muttaqi.android.feature.topics.ExploreRoute
import com.muttaqi.android.feature.topics.TopicPageRoute
import com.muttaqi.android.navigation.ExploreTab
import com.muttaqi.android.navigation.HomeTab
import com.muttaqi.android.navigation.QuranTab
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Home's ways into the Quran and Explore switch tabs, as iOS's router does */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class HomeNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var navController: NavHostController

    private fun tabs() = compose.setContent {
        navController = rememberNavController()
        // The app's tabs, as MuttaqiApp lays them out, with empty screens
        NavHost(navController, startDestination = HomeTab) {
            navigation<HomeTab>(startDestination = HomeRoute) { composable<HomeRoute> {} }
            navigation<ExploreTab>(startDestination = ExploreRoute) { composable<ExploreRoute> {} }
            navigation<QuranTab>(startDestination = QuranListRoute) {
                composable<QuranListRoute> {}
                composable<SurahRoute> {}
            }
            composable<TopicPageRoute> {}
        }
    }

    private fun onTab(tab: Any) = navController.currentDestination?.hierarchy?.any { it.hasRoute(tab::class) } == true

    @Test
    fun continuingReadingOpensTheSurahInTheQuranTab() {
        tabs()
        compose.runOnIdle { navController.openInTab(QuranTab, SurahRoute(16, 127)) }
        compose.runOnIdle {
            assertEquals(SurahRoute(16, 127), navController.currentBackStackEntry!!.toRoute<SurahRoute>())
            assertTrue(onTab(QuranTab))
            // Back goes to the Quran tab's list, then Home, as on iOS
            navController.popBackStack()
        }
        compose.runOnIdle {
            assertTrue(navController.currentDestination!!.hasRoute(QuranListRoute::class))
            navController.popBackStack()
        }
        compose.runOnIdle { assertTrue(navController.currentDestination!!.hasRoute(HomeRoute::class)) }
    }

    @Test
    fun theTopicOfTheDayOpensInTheExploreTab() {
        tabs()
        compose.runOnIdle { navController.openInTab(ExploreTab, TopicPageRoute(TopicChips.ExploreGroup, "health-sickness")) }
        compose.runOnIdle {
            assertEquals(TopicPageRoute(TopicChips.ExploreGroup, "health-sickness"), navController.currentBackStackEntry!!.toRoute<TopicPageRoute>())
            navController.popBackStack()
        }
        compose.runOnIdle { assertTrue(navController.currentDestination!!.hasRoute(ExploreRoute::class)) }
    }
}
