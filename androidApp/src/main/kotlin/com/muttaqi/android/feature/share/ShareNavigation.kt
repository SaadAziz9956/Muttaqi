package com.muttaqi.android.feature.share

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.shared.core.share.SharePassage
import kotlinx.serialization.Serializable

/** The Share page for a passage; open it with `navController.navigate(ShareRoute(passage))` */
@Serializable
data class ShareRoute(val arabic: String, val transliteration: String?, val translation: String, val reference: String) {
    constructor(passage: SharePassage) : this(passage.arabic, passage.transliteration, passage.translation, passage.reference)

    val passage: SharePassage get() = SharePassage(arabic, transliteration, translation, reference)
}

/** Registered once, outside the tabs, since every feature can share */
fun NavGraphBuilder.shareDestinations(navController: NavController) {
    composable<ShareRoute> { entry ->
        ShareRoute(passage = entry.toRoute<ShareRoute>().passage, onBack = navController::popBackStack)
    }
}
