package com.muttaqi.android.feature.home

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.StatusBarFade
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.feature.prayer.NextPrayerPill
import com.muttaqi.android.feature.prayer.PrayerTimesStrip
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.home.presentation.HomeEffect
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeState
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import java.time.ZoneId
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home, wired to its shared view model. It refreshes each time it comes back into view, e.g. from the Dikr counter or
 * the background, and the compass follows the phone only while Home is on screen
 */
@Composable
fun HomeRoute(
    onOpenQibla: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenNames: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenJournalEntry: (String?) -> Unit,
    onOpenEmotions: () -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenSurah: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onShare: (SharePassage) -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val qiblaArrow = viewModel.qiblaArrow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    LifecycleResumeEffect(viewModel) {
        viewModel.dispatch(HomeIntent.Refresh)
        onPauseOrDispose {}
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.OpenQibla -> onOpenQibla()
                HomeEffect.OpenDhikr -> onOpenDhikr()
                HomeEffect.OpenNames -> onOpenNames()
                HomeEffect.OpenJournal -> onOpenJournal()
                is HomeEffect.OpenJournalEntry -> onOpenJournalEntry(effect.entryId)
                HomeEffect.OpenEmotions -> onOpenEmotions()
                is HomeEffect.OpenTopic -> onOpenTopic(effect.topicId)
                is HomeEffect.OpenSurah -> onOpenSurah(effect.surah.number, effect.ayahNumber)
                is HomeEffect.OpenShare -> onShare(effect.passage)
                is HomeEffect.Copy -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Muttaqi", effect.text))) }
                HomeEffect.OpenSettings -> context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            }
        }
    }
    // The arrow is read inside the Qibla tile only, so each compass reading redraws the pointer and nothing else
    HomeScreen(state, qiblaArrow = { qiblaArrow.value }, onIntent = viewModel::dispatch)
}

/**
 * The Hijri date and the next prayer, the greeting and its verse, today's prayer times, the bento tiles, the ways back
 * into the Quran and the day's ayah, hadith and dua, as on iOS
 */
@Composable
fun HomeScreen(
    state: HomeState,
    qiblaArrow: () -> Double?,
    onIntent: (HomeIntent) -> Unit,
    scrollState: ScrollState = rememberScrollState(),
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val soft = MuttaqiTheme.soft
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrolled by remember(scrollState) { derivedStateOf { scrollState.value > 0 } }
    Box(Modifier.fillMaxSize()) {
        SoftBackdrop()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp + bottomInset),
        ) {
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(state.hijriDate, style = MaterialTheme.typography.bodySmall, color = soft.appPrimary)
                Spacer(Modifier.weight(1f).widthIn(min = 8.dp))
                NextPrayerPill(
                    upcoming = state.nextPrayer,
                    needsLocation = state.asksForLocation,
                    onSetLocation = { onIntent(HomeIntent.SetLocationTapped) },
                    zone = zone,
                )
            }

            Greeting(state, Modifier.padding(top = 30.dp))

            state.schedule?.today?.let { today ->
                // Once Isha has passed, the next prayer is tomorrow's Fajr, so nothing in today's row is picked
                PrayerTimesStrip(today, next = state.nextPrayerToday, modifier = Modifier.padding(top = 28.dp), zone = zone)
            }

            HomeBento(state, qiblaArrow, onIntent, Modifier.padding(top = 18.dp))

            Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.lastReading?.let { reading ->
                    SurahShortcut(reading.surah.number, "Continue ${reading.surah.englishName}", "Ayah ${reading.ayahNumber}") {
                        onIntent(HomeIntent.ContinueReadingTapped)
                    }
                }
                // Reading al-Kahf on Friday is a sunnah, so on Fridays it's a tap away
                state.fridayKahf?.let { kahf ->
                    SurahShortcut(kahf.number, "Surah ${kahf.englishName}", "Friday") { onIntent(HomeIntent.KahfTapped) }
                }
            }

            Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                state.ayahOfTheDay?.let { AyahOfTheDayCard(it, onIntent) }
                state.hadithOfTheDay?.let { HadithOfTheDayCard(it, onIntent) }
                state.duaOfTheDay?.let { DuaOfTheDayCard(it, onIntent) }
            }
        }
        // The page fades out under the status bar once scrolled, so text never runs behind the clock
        StatusBarFade(visible = scrolled)
    }
}

@Composable
private fun Greeting(state: HomeState, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Assalam - o - Alaikum", style = MaterialTheme.typography.headlineMedium, color = soft.appPrimary)
        val quote = state.greeting
        val translation = quote?.ayah?.translation
        if (quote != null && translation != null) {
            TranslationText(translation.quoted(), Modifier.padding(top = 20.dp), fontSize = 14.sp, lineSpacing = 0.sp)
            Text(
                "Quran (${quote.reference})",
                Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = soft.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
