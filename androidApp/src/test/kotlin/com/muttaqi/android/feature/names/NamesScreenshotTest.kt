package com.muttaqi.android.feature.names

import android.app.Application
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.content.AssetContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.feature.names.presentation.NamesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The 99 Names page and its search with the real bundled names, to compare with the iOS screenshots */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PHONE)
class NamesScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun names(language: Language) =
        runBlocking { BundledNamesRepository(AssetContentSource(RuntimeEnvironment.getApplication()), Immediately).names(language) }

    @Test
    fun page() {
        val names = names(Language.English)
        val state = NamesState(isLoading = false, names = names, hadith = PageQuotes.ninetyNineNames.displayed(Language.English))
        compose.captureLightAndDark("names") {
            NamesScreen(state, rememberPagerState(initialPage = 1) { names.size }, position = 2, onIntent = {}, onOpenSearch = {}, onBack = {})
        }
    }

    @Test
    fun searchByNameInUrdu() {
        val names = names(Language.Urdu)
        val results = BuildNamesSearchIndex(SearchTextFolder)(names).search("rahman", NameSearchMode.ByName)
        val state = NamesState(isLoading = false, names = names, query = "rahman", searchMode = NameSearchMode.ByName, results = results)
        compose.captureLightAndDark("names_search_urdu") { NamesSearchScreen(state, onIntent = {}, onBack = {}) }
    }
}

/** Runs the repository's reads where they're called, so a test can load the texts before drawing */
private object Immediately : DispatcherProvider {
    override val main = Dispatchers.Unconfined
    override val io = Dispatchers.Unconfined
    override val default = Dispatchers.Unconfined
}
