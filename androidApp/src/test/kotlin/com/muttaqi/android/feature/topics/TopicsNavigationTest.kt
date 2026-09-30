package com.muttaqi.android.feature.topics

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class TopicsNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theTopicPageRouteCarriesWhichTopicsItsChipsShow() {
        lateinit var navController: NavHostController
        val opened = mutableListOf<TopicPageRoute>()
        compose.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = EmotionsRoute) {
                composable<EmotionsRoute> {}
                composable<TopicPageRoute> { opened += it.toRoute<TopicPageRoute>() }
            }
        }
        compose.runOnIdle { navController.navigate(TopicPageRoute(TopicChips.ExploreGroup, "prophet-muhammad")) }
        compose.waitForIdle()
        assertEquals(TopicPageRoute(TopicChips.ExploreGroup, "prophet-muhammad"), opened.last())
    }
}
