package com.muttaqi.android.feature.quran

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.MushafPage
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.SurahReading
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import com.muttaqi.shared.feature.quran.presentation.arabicWithoutEndSign
import com.muttaqi.shared.feature.quran.presentation.reader.SurahDirection
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderContent
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderEffect
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderIntent
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderState
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderViewModel
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsViewModel
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirIntent
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderRoute(surahNumber: Int, startAyah: Int, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<SurahReaderViewModel> { parametersOf(surahNumber, startAyah) }
    val settings = koinViewModel<ReadingSettingsViewModel>()
    val tafsir = koinViewModel<TafsirViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val position by viewModel.readingPosition.collectAsStateWithLifecycle()
    val settingsState by settings.state.collectAsStateWithLifecycle()
    val tafsirState by tafsir.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLanguages by rememberSaveable { mutableStateOf(false) }
    var tafsirStart by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SurahReaderEffect.OpenTafsir -> {
                    tafsir.dispatch(TafsirIntent.Opened(effect.surahNumber))
                    tafsirStart = effect.startAyah ?: 0
                }
                is SurahReaderEffect.OpenShare -> onShare(effect.passage)
                is SurahReaderEffect.Copy -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Ayah", effect.text))) }
            }
        }
    }
    DisposableEffect(viewModel) { onDispose { viewModel.dispatch(SurahReaderIntent.Left) } }

    SurahReaderScreen(
        state = state,
        readingPosition = position,
        onIntent = viewModel::dispatch,
        onSettings = { showSettings = true },
        onBack = onBack,
    )

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }, containerColor = MuttaqiTheme.soft.canvas) {
            ReadingSettingsContent(settingsState, settings::dispatch, onChooseLanguage = { showLanguages = true })
        }
    }
    if (showLanguages) {
        ModalBottomSheet(onDismissRequest = { showLanguages = false }, containerColor = MuttaqiTheme.soft.canvas) {
            LanguagePickerContent(settingsState, onSelect = {
                settings.dispatch(it)
                showLanguages = false
            })
        }
    }
    tafsirStart?.let { start ->
        ModalBottomSheet(
            onDismissRequest = { tafsirStart = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MuttaqiTheme.soft.canvas,
        ) {
            TafsirContent(
                surah = state.headerSurah,
                state = tafsirState,
                startAyah = start.takeIf { it > 0 },
                onRetry = { tafsir.dispatch(TafsirIntent.Retry) },
            )
        }
    }
}

@Composable
fun SurahReaderScreen(
    state: SurahReaderState,
    readingPosition: String?,
    onIntent: (SurahReaderIntent) -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
) {
    Box {
        SoftBackdrop()
        AnimatedContent(
            targetState = state,
            contentKey = { it.surahNumber },
            transitionSpec = {
                val forward = targetState.direction == SurahDirection.Forward
                slideInHorizontally { if (forward) it else -it } togetherWith slideOutHorizontally { if (forward) -it else it }
            },
            label = "surah",
        ) { page ->
            SurahPage(page, onIntent, onSettings, onBack)
        }
        AnimatedVisibility(
            readingPosition != null,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ReadingPositionPill(readingPosition.orEmpty())
        }
    }
}

@Composable
private fun SurahPage(state: SurahReaderState, onIntent: (SurahReaderIntent) -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    val list = rememberLazyListState()
    val header = state.headerSurah
    TrackVisibleAyahs(list, onIntent)
    ScrollToStart(state, list, onIntent)
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SoftTopBar(
                title = header?.englishName.orEmpty(),
                showTitle = list.firstVisibleItemIndex > 0,
                onBack = onBack,
                actions = {
                    SoftIconButton(R.drawable.ic_setting_4_linear, "Reading settings", onSettings, Modifier.padding(end = 12.dp), size = 44.dp, iconSize = 22.dp)
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = list,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding()),
        ) {
            item(key = "header") {
                SurahHeader(header, state.previousSurah, state.nextSurah, onIntent)
            }
            when (val content = state.content) {
                SurahReaderContent.Loading -> item(key = "loading") { Loading(Modifier.padding(top = 100.dp)) }
                is SurahReaderContent.Failed -> item(key = "failed") {
                    LoadFailed(
                        content.message,
                        onRetry = { onIntent(SurahReaderIntent.Retry) },
                        modifier = Modifier.padding(top = 100.dp).height(360.dp),
                        suggestion = content.suggestion,
                    )
                }
                is SurahReaderContent.Loaded -> loadedContent(content.reading, state, onIntent)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.loadedContent(
    reading: SurahReading,
    state: SurahReaderState,
    onIntent: (SurahReaderIntent) -> Unit,
) {
    if (reading.showsBismillah) {
        item(key = "bismillah") { Arriving { Bismillah(reading.bismillahText, reading.bismillahTranslation) } }
    }
    when (state.settings.mode) {
        ReadingMode.WithTranslation -> items(reading.ayahs, key = { it.number }) { ayah ->
            Arriving {
                AyahCard(
                    ayah = ayah,
                    fontSize = state.settings.fontSize,
                    language = state.settings.language,
                    onExplanation = { onIntent(SurahReaderIntent.AyahExplanationTapped(ayah.numberInSurah)) },
                    onCopy = { onIntent(SurahReaderIntent.CopyTapped(ayah.number)) },
                    onShare = { onIntent(SurahReaderIntent.ShareTapped(ayah.number)) },
                )
            }
        }
        ReadingMode.ArabicOnly -> {
            item(key = "pages-top") { Spacer(Modifier.height(16.dp)) }
            items(reading.pages, key = { it.id }) { page -> Arriving { MushafPageCard(page, state.settings.fontSize) } }
        }
    }
    item(key = "end") {
        Arriving {
            Column(Modifier.padding(top = 24.dp, bottom = 72.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SurahEndNavigation(reading.previousSurah, reading.nextSurah, onIntent)
                Text(
                    QuranMessages.MUSHAF_CREDIT + "\n" + QuranMessages.translationCredit(state.settings.language),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MuttaqiTheme.soft.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.Arriving(content: @Composable () -> Unit) {
    Box(Modifier.animateItem(fadeInSpec = tween(220), placementSpec = null, fadeOutSpec = null)) { content() }
}

@Composable
private fun TrackVisibleAyahs(list: LazyListState, onIntent: (SurahReaderIntent) -> Unit) {
    LaunchedEffect(list) {
        snapshotFlow {
            val info = list.layoutInfo
            val top = info.viewportStartOffset
            val bottom = info.viewportEndOffset
            info.visibleItemsInfo.mapNotNull { item ->
                val id = item.key as? Int ?: return@mapNotNull null
                val shown = minOf(bottom, item.offset + item.size) - maxOf(top, item.offset)
                id.takeIf { item.size > 0 && shown >= item.size * 0.2f }
            }
        }.distinctUntilChanged().collect { onIntent(SurahReaderIntent.VisibleAyahsChanged(it)) }
    }
}

@Composable
private fun ScrollToStart(state: SurahReaderState, list: LazyListState, onIntent: (SurahReaderIntent) -> Unit) {
    val target = state.startScrollTarget
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        repeat(2) {
            list.scrollToItem(itemIndex(state, target))
            delay(300)
        }
        onIntent(SurahReaderIntent.ReachedStart)
    }
}

private fun itemIndex(state: SurahReaderState, target: Int): Int {
    val reading = state.reading ?: return 0
    val leading = 1 + (if (reading.showsBismillah) 1 else 0)
    return when (state.settings.mode) {
        ReadingMode.WithTranslation -> leading + reading.ayahs.indexOfFirst { it.number == target }.coerceAtLeast(0)
        ReadingMode.ArabicOnly -> leading + 1 + reading.pages.indexOfFirst { it.id == target }.coerceAtLeast(0)
    }
}

@Composable
private fun SurahHeader(surah: Surah?, previous: Surah?, next: Surah?, onIntent: (SurahReaderIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (surah != null) {
            QuranText(surah.name, fontSize = 40.sp, lineSpacing = 0.sp)
        }
        Text(
            surah?.englishName.orEmpty(),
            Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, fontSize = 26.sp),
            color = soft.appPrimary,
        )
        if (surah != null) {
            Text(
                "${surah.englishNameTranslation} · ${surah.revelationType} · ${surah.numberOfAyahs} ayahs",
                Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = soft.textSecondary,
            )
        }
        Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            previous?.let { NeighbourPill(it, isNext = false) { onIntent(SurahReaderIntent.PreviousTapped) } }
            SoftPillSurface(Modifier.height(36.dp), fill = soft.brandGreen, rim = false, onClick = { onIntent(SurahReaderIntent.ExplanationTapped) }) {
                Text("Explanation", Modifier.padding(horizontal = 18.dp), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = Color.White)
            }
            next?.let { NeighbourPill(it, isNext = true) { onIntent(SurahReaderIntent.NextTapped) } }
        }
    }
}

@Composable
private fun NeighbourPill(neighbour: Surah, isNext: Boolean, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftPillSurface(Modifier.height(36.dp), onClick = onClick) {
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!isNext) Icon(painterResource(R.drawable.ic_arrow_left_02_linear), null, Modifier.size(14.dp), tint = soft.appPrimary)
            Text(neighbour.englishName, style = MaterialTheme.typography.labelSmall, color = soft.appPrimary, maxLines = 1)
            if (isNext) Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.size(14.dp), tint = soft.appPrimary)
        }
    }
}

@Composable
private fun Bismillah(text: String, translation: String?) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        QuranText(text, Modifier.padding(top = 44.dp, bottom = if (translation == null) 20.dp else 0.dp), fontSize = 18.sp, lineSpacing = 0.sp)
        translation?.let {
            val english = !it.isArabicScript() && it.none { char -> char.code in 0x0900..0x097F }
            TranslationText(
                it,
                Modifier.padding(top = 18.dp, bottom = 20.dp).padding(horizontal = if (english) 60.dp else 16.dp),
                fontSize = if (english) 12.sp else 13.sp,
                color = soft.textSecondary,
                lineSpacing = 0.sp,
            )
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    fontSize: FontSize,
    language: Language,
    onExplanation: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val soft = MuttaqiTheme.soft
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1_500)
            copied = false
        }
    }
    SoftCard(Modifier.fillMaxWidth().padding(top = 14.dp), cornerRadius = 26.dp) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            AyahArabic(ayah, fontSize)
            ayah.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(
                    it,
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    fontSize = fontSize.transliterationSize.sp,
                    color = soft.appPrimary,
                    textAlign = TextAlign.Left,
                    lineSpacing = 0.sp,
                )
            }
            ayah.translation?.takeIf { it.isNotEmpty() }?.let { translation ->
                if (language == Language.Urdu) {
                    TranslationText(
                        translation,
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        fontSize = (fontSize.translationSize - 1).sp,
                        textAlign = TextAlign.Right,
                        lineSpacing = 0.sp,
                    )
                } else {
                    TranslationText(
                        "${ayah.numberInSurah}.  $translation",
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        fontSize = fontSize.translationSize.sp,
                        textAlign = TextAlign.Left,
                        lineSpacing = 0.sp,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ayah.reference,
                    Modifier.background(soft.tintedSurface, CircleShape).padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = soft.brandTeal,
                )
                Spacer(Modifier.weight(1f))
                SoftIconButton(R.drawable.ic_book_linear, "Explanation", onExplanation, size = 34.dp, iconSize = 16.dp)
                SoftIconButton(
                    if (copied) R.drawable.ic_tick_circle_linear else R.drawable.ic_copy_linear,
                    if (copied) "Copied" else "Copy",
                    onClick = {
                        onCopy()
                        copied = true
                    },
                    size = 34.dp,
                    iconSize = 16.dp,
                )
                SoftIconButton(R.drawable.ic_export_arrow_01_linear, "Share", onShare, size = 34.dp, iconSize = 16.dp)
            }
        }
    }
}

@Composable
private fun AyahArabic(ayah: Ayah, fontSize: FontSize) {
    val soft = MuttaqiTheme.soft
    val text = remember(ayah.number, soft.appPrimary) {
        buildAnnotatedString {
            append(ayah.arabicWithoutEndSign())
            append(' ')
            withStyle(SpanStyle(color = soft.appPrimary, fontSize = 10.sp, fontFamily = NastaliqFont)) { append('﴿') }
            withStyle(SpanStyle(color = soft.appPrimary, fontSize = 14.sp)) { append("${ayah.numberInSurah}") }
            withStyle(SpanStyle(color = soft.appPrimary, fontSize = 10.sp, fontFamily = NastaliqFont)) { append('﴾') }
        }
    }
    Text(
        text,
        Modifier.fillMaxWidth().padding(top = 6.dp),
        color = soft.textPrimary,
        textAlign = TextAlign.Right,
        style = TextStyle(fontFamily = QuranFont, fontSize = fontSize.arabicSize.sp, textDirection = TextDirection.Rtl),
    )
}

@Composable
fun MushafPageCard(page: MushafPage, fontSize: FontSize) {
    val soft = MuttaqiTheme.soft
    val text = remember(page.id, soft.appPrimary) {
        buildAnnotatedString {
            page.ayahs.forEachIndexed { index, ayah ->
                append(ayah.arabicWithoutEndSign())
                append('\u00A0')
                withStyle(SpanStyle(color = soft.appPrimary)) { append(ayah.ayahMark) }
                if (index < page.ayahs.lastIndex) append(' ')
            }
        }
    }
    SoftCard(Modifier.fillMaxWidth().padding(vertical = 8.dp), cornerRadius = 26.dp) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text,
                Modifier.fillMaxWidth(),
                color = soft.textPrimary,
                style = TextStyle(
                    fontFamily = QuranFont,
                    fontSize = fontSize.arabicSize.sp,
                    lineHeight = (QURAN_FONT_LINE_HEIGHT + MUSHAF_LINE_SPACING).em,
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both),
                    textAlign = TextAlign.Justify,
                    textDirection = TextDirection.Rtl,
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Hairline(Modifier.weight(1f))
                Text("${page.number}", style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
                Hairline(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(if (MuttaqiTheme.soft.dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)))
}

private const val QURAN_FONT_LINE_HEIGHT = 1.758f
private const val MUSHAF_LINE_SPACING = 0.6f


@Composable
private fun SurahEndNavigation(previous: Surah?, next: Surah?, onIntent: (SurahReaderIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) {
            previous?.let { EndCard(it, isNext = false) { onIntent(SurahReaderIntent.PreviousTapped) } }
        }
        Box(Modifier.weight(1f)) {
            next?.let { EndCard(it, isNext = true) { onIntent(SurahReaderIntent.NextTapped) } }
        }
    }
}

@Composable
private fun EndCard(neighbour: Surah, isNext: Boolean, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftCard(Modifier.fillMaxWidth(), cornerRadius = 24.dp, onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = if (isNext) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(if (isNext) "Next" else "Previous", style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isNext) Icon(painterResource(R.drawable.ic_arrow_left_02_linear), null, Modifier.size(16.dp), tint = soft.textPrimary)
                Text(neighbour.englishName, style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isNext) Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.size(16.dp), tint = soft.textPrimary)
            }
        }
    }
}

@Composable
private fun ReadingPositionPill(position: String) {
    SoftPillSurface(Modifier.height(36.dp), fill = MuttaqiTheme.soft.surface.copy(alpha = 0.96f)) {
        AnimatedContent(position, label = "position") {
            Text(it, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = MuttaqiTheme.soft.appPrimary)
        }
    }
}

@Composable
internal fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        DelayedLoadingIndicator()
    }
}
