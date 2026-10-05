package com.muttaqi.android.feature.home

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.feature.prayer.PrayerTimesCard
import com.muttaqi.android.feature.prayer.SetLocationButton
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.home.presentation.HomeEffect
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeState
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import java.time.ZoneId
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

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
    HomeScreen(state, qiblaArrow = { qiblaArrow.value }, onIntent = viewModel::dispatch)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    qiblaArrow: () -> Double?,
    onIntent: (HomeIntent) -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Assalam - o - Alaikum") },
                subtitle = { Text(state.hijriDate) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.greeting?.let { greeting ->
                greeting.ayah.translation?.let { translation ->
                    item(key = "greeting") { PageQuote(DisplayedQuote(translation, "Quran (${greeting.reference})")) }
                }
            }
            item(key = "prayer") {
                val upcoming = state.nextPrayer
                val today = state.schedule?.today
                Column {
                    AnimatedVisibility(
                        upcoming != null || today != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        PrayerTimesCard(upcoming, today, state.nextPrayerToday, zone = zone)
                    }
                    if (state.asksForLocation) SetLocationButton(onClick = { onIntent(HomeIntent.SetLocationTapped) })
                }
            }
            item(key = "tiles") { HomeTiles(state, qiblaArrow, onIntent) }
            state.lastReading?.let { reading ->
                item(key = "continue") {
                    SurahShortcut(reading.surah.number, "Continue ${reading.surah.englishName}", "Ayah ${reading.ayahNumber}") {
                        onIntent(HomeIntent.ContinueReadingTapped)
                    }
                }
            }
            state.fridayKahf?.let { kahf ->
                item(key = "kahf") {
                    SurahShortcut(kahf.number, "Surah ${kahf.englishName}", "Friday") { onIntent(HomeIntent.KahfTapped) }
                }
            }
            state.ayahOfTheDay?.let { item(key = "ayah") { AyahOfTheDayCard(it, onIntent) } }
            state.hadithOfTheDay?.let { item(key = "hadith") { HadithOfTheDayCard(it, onIntent) } }
            state.duaOfTheDay?.let { item(key = "dua") { DuaOfTheDayCard(it, onIntent) } }
        }
    }
}
