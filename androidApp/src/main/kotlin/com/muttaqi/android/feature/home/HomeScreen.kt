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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.LocalTabBarInset
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.android.feature.prayer.PrayerTimesCard
import com.muttaqi.android.feature.prayer.SetLocationButton
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.quoted
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

@Composable
fun HomeScreen(
    state: HomeState,
    qiblaArrow: () -> Double?,
    onIntent: (HomeIntent) -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val greeting = state.greeting?.let { greeting -> greeting.ayah.translation?.let { DisplayedQuote(it, "Quran (${greeting.reference})") } }
    OneUiSurface {
        OneUiScaffold(
            title = "Assalam - o - Alaikum",
            subtitle = if (state.hijriDate.isNotBlank() || greeting != null) {
                { HomeHeader(state.hijriDate, greeting) }
            } else {
                null
            },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = OneUiDefaults.ScreenMargin,
                    end = OneUiDefaults.ScreenMargin,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + LocalTabBarInset.current + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing),
            ) {
                item(key = "prayer") {
                    val upcoming = state.nextPrayer
                    val today = state.schedule?.today
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                val shortcuts = listOfNotNull(
                    state.lastReading?.let { reading ->
                        SurahShortcut(reading.surah.number, "Continue ${reading.surah.englishName}", "Ayah ${reading.ayahNumber}") {
                            onIntent(HomeIntent.ContinueReadingTapped)
                        }
                    },
                    state.fridayKahf?.let { kahf ->
                        SurahShortcut(kahf.number, "Surah ${kahf.englishName}", "Friday") { onIntent(HomeIntent.KahfTapped) }
                    },
                )
                if (shortcuts.isNotEmpty()) item(key = "shortcuts") { SurahShortcuts(shortcuts) }
                state.ayahOfTheDay?.let { item(key = "ayah") { AyahOfTheDayCard(it, onIntent) } }
                state.hadithOfTheDay?.let { item(key = "hadith") { HadithOfTheDayCard(it, onIntent) } }
                state.duaOfTheDay?.let { item(key = "dua") { DuaOfTheDayCard(it, onIntent) } }
            }
        }
    }
}

@Composable
private fun HomeHeader(hijriDate: String, greeting: DisplayedQuote?) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (hijriDate.isNotBlank()) {
            Text(hijriDate, style = type.listTitle, color = colors.secondaryText, textAlign = TextAlign.Center)
        }
        greeting?.let { quote ->
            TranslationText(
                quote.text.quoted(),
                Modifier.fillMaxWidth().padding(top = if (hijriDate.isNotBlank()) 14.dp else 0.dp),
                style = type.listSummary,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(quote.source, style = type.small, color = colors.accent, textAlign = TextAlign.Center)
        }
    }
}
