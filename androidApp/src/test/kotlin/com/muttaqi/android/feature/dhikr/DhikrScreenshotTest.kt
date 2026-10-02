package com.muttaqi.android.feature.dhikr

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.content.AssetContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterState
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PHONE)
class DhikrScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate(2026, 9, 30)

    private fun sections(language: Language) =
        runBlocking { BundledDhikrRepository(AssetContentSource(RuntimeEnvironment.getApplication()), Immediately).sections(language) }

    private fun dhikr(id: String, language: Language = Language.English): Dhikr =
        sections(language).flatMap { it.dhikr }.first { it.id == id }

    @Test
    fun list() {
        val state = DhikrListState(isLoading = false, header = PageQuotes.rememberingAllah.displayed(Language.English), sections = sections(Language.English))
        compose.captureLightAndDark("dhikr_list") { DhikrListScreen(state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun counter() {
        val state = DhikrCounterState(isLoading = false, dhikr = dhikr("subhanallahi-wa-bihamdihi"), progress = DhikrProgress(7, 0, today))
        compose.captureLightAndDark("dhikr_counter") { DhikrCounterScreen(state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun counterForASet() {
        val state = DhikrCounterState(isLoading = false, dhikr = dhikr("after-every-prayer-33-33-and"), progress = DhikrProgress(40, 0, today))
        compose.captureLightAndDark("dhikr_counter_set") { DhikrCounterScreen(state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun aCompleteRoundInUrdu() {
        val dhikr = dhikr("five-declarations-of-oneness", Language.Urdu)
        val state = DhikrCounterState(isLoading = false, dhikr = dhikr, progress = DhikrProgress(5, 1, today))
        compose.captureLightAndDark("dhikr_counter_complete_urdu") { DhikrCounterScreen(state, onIntent = {}, onBack = {}) }
    }
}

private object Immediately : DispatcherProvider {
    override val main = Dispatchers.Unconfined
    override val io = Dispatchers.Unconfined
    override val default = Dispatchers.Unconfined
}
