package com.muttaqi.android.feature.journal

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryState
import com.muttaqi.shared.feature.journal.presentation.list.JournalListState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.TimeZone
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = "en-rPK-$PHONE")
class JournalScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun setUp() = TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"))

    private fun entry(id: String, title: String, body: String, at: String) =
        Instant.parse(at).let { JournalEntry(id, title, body, it, it) }

    private val walk = entry("walk", "Morning light", "Grateful for the quiet walk before Fajr and the cool air.", "2026-09-30T05:10:00Z")
    private val parents = entry("parents", "", "Alhamdulillah for my parents' health.\nAnd for the rain this evening.", "2026-09-29T14:00:00Z")
    private val eid = entry("eid", "Eid with the whole family", "Everyone came, even the cousins from Lahore.", "2025-06-07T09:00:00Z")
    private val quote = DisplayedQuote("Nun. By the pen and what they inscribe", "Quran (68:1)")

    @Test
    fun list() = compose.captureLightAndDark("journal_list") {
        JournalListScreen(JournalListState(isLoading = false, header = quote, entries = listOf(walk, parents, eid)), onIntent = {}, onBack = {})
    }

    @Test
    fun listUrdu() = compose.captureLightAndDark("journal_list_urdu") {
        JournalListScreen(
            JournalListState(isLoading = false, header = PageQuotes.byThePen.displayed(Language.Urdu), entries = listOf(walk)),
            onIntent = {},
            onBack = {},
        )
    }

    @Test
    fun listEmpty() = compose.captureLightAndDark("journal_list_empty") {
        JournalListScreen(JournalListState(isLoading = false, header = quote), onIntent = {}, onBack = {})
    }

    @Test
    fun listSearching() = compose.captureLightAndDark("journal_list_search") {
        JournalListScreen(
            JournalListState(isLoading = false, header = quote, entries = listOf(walk, parents, eid), query = "Rain", results = listOf(parents)),
            onIntent = {},
            onBack = {},
        )
    }

    @Test
    fun entry() = compose.captureLightAndDark("journal_entry") {
        val body = walk.body + " We talked for an hour and laughed about old times at home. Then we planned a visit for the winter holidays."
        JournalEntryScreen(JournalEntryState(entry = walk.copy(body = body)), onIntent = {}, onBack = {})
    }

    @Test
    fun newEntry() = compose.captureLightAndDark("journal_entry_new") {
        JournalEntryScreen(JournalEntryState(entry = walk.copy(title = "", body = ""), startedEmpty = true), onIntent = {}, onBack = {})
    }
}
