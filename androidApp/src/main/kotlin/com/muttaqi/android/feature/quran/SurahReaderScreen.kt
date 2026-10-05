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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
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
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            ReadingSettingsContent(settingsState, settings::dispatch)
        }
    }
    tafsirStart?.let { start ->
        ModalBottomSheet(
            onDismissRequest = { tafsirStart = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
    Box(Modifier.fillMaxSize()) {
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
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 16.dp),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ReadingPosition(readingPosition.orEmpty())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahPage(state: SurahReaderState, onIntent: (SurahReaderIntent) -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    val list = rememberLazyListState()
    val header = state.headerSurah
    TrackVisibleAyahs(list, onIntent)
    ScrollToStart(state, list, onIntent)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val showTitle by remember(list) { derivedStateOf { list.firstVisibleItemIndex > 0 } }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(showTitle, enter = fadeIn(), exit = fadeOut()) {
                        Text(header?.englishName.orEmpty())
                    }
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(painterResource(R.drawable.ic_tune), contentDescription = "Reading settings")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            state = list,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

private fun LazyListScope.loadedContent(
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
            item(key = "pages-top") { Spacer(Modifier.height(4.dp)) }
            items(reading.pages, key = { it.id }) { page -> Arriving { MushafPageCard(page, state.settings.fontSize) } }
        }
    }
    item(key = "end") {
        Arriving {
            Column(
                Modifier.navigationBarsPadding().padding(top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SurahEndNavigation(reading.previousSurah, reading.nextSurah, onIntent)
                Text(
                    QuranMessages.MUSHAF_CREDIT + "\n" + QuranMessages.translationCredit(state.settings.language),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (surah != null) {
            ArabicText(surah.name, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        }
        Text(surah?.englishName.orEmpty(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        if (surah != null) {
            Text(
                "${surah.englishNameTranslation} · ${surah.revelationType} · ${surah.numberOfAyahs} ayahs",
                Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        FilledTonalButton(onClick = { onIntent(SurahReaderIntent.ExplanationTapped) }, Modifier.padding(top = 16.dp)) {
            Icon(painterResource(R.drawable.ic_book), contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Explanation")
        }
        if (previous != null || next != null) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                previous?.let { NeighbourButton(it, isNext = false) { onIntent(SurahReaderIntent.PreviousTapped) } }
                Spacer(Modifier.weight(1f))
                next?.let { NeighbourButton(it, isNext = true) { onIntent(SurahReaderIntent.NextTapped) } }
            }
        }
    }
}

@Composable
private fun NeighbourButton(neighbour: Surah, isNext: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        if (!isNext) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(neighbour.englishName)
        if (isNext) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
        }
    }
}

@Composable
private fun Bismillah(text: String, translation: String?) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuranText(text, Modifier.fillMaxWidth(), lineSpacing = 0.sp)
        translation?.let {
            TranslationText(
                it,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1_500)
            copied = false
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            AyahArabic(ayah, fontSize)
            ayah.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(
                    it,
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    fontSize = fontSize.transliterationSize.sp,
                    color = MaterialTheme.colorScheme.primary,
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
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ayah.reference,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onExplanation) {
                    Icon(painterResource(R.drawable.ic_book), contentDescription = "Explanation")
                }
                IconButton(
                    onClick = {
                        onCopy()
                        copied = true
                    },
                ) {
                    Icon(
                        painterResource(if (copied) R.drawable.ic_check else R.drawable.ic_content_copy),
                        contentDescription = if (copied) "Copied" else "Copy",
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
                }
            }
        }
    }
}

@Composable
private fun AyahArabic(ayah: Ayah, fontSize: FontSize) {
    val markColor = MaterialTheme.colorScheme.primary
    val text = remember(ayah.number, markColor) {
        buildAnnotatedString {
            append(ayah.arabicWithoutEndSign())
            append(' ')
            withStyle(SpanStyle(color = markColor, fontSize = 10.sp, fontFamily = NastaliqFont)) { append('﴿') }
            withStyle(SpanStyle(color = markColor, fontSize = 14.sp)) { append("${ayah.numberInSurah}") }
            withStyle(SpanStyle(color = markColor, fontSize = 10.sp, fontFamily = NastaliqFont)) { append('﴾') }
        }
    }
    Text(
        text,
        Modifier.fillMaxWidth().padding(top = 6.dp),
        textAlign = TextAlign.Right,
        style = TextStyle(fontFamily = QuranFont, fontSize = fontSize.arabicSize.sp, textDirection = TextDirection.Rtl),
    )
}

@Composable
fun MushafPageCard(page: MushafPage, fontSize: FontSize) {
    val markColor = MaterialTheme.colorScheme.primary
    val text = remember(page.id, markColor) {
        buildAnnotatedString {
            page.ayahs.forEachIndexed { index, ayah ->
                append(ayah.arabicWithoutEndSign())
                append(' ')
                withStyle(SpanStyle(color = markColor)) { append(ayah.ayahMark) }
                if (index < page.ayahs.lastIndex) append(' ')
            }
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text,
                Modifier.fillMaxWidth(),
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
                HorizontalDivider(Modifier.weight(1f))
                Text("${page.number}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.weight(1f))
            }
        }
    }
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
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = if (isNext) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                if (isNext) "Next" else "Previous",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isNext) Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, Modifier.size(18.dp))
                Text(neighbour.englishName, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.titleSmall)
                if (isNext) Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReadingPosition(position: String) {
    HorizontalFloatingToolbar(expanded = true, colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()) {
        AnimatedContent(position, Modifier.align(Alignment.CenterVertically), label = "position") {
            Text(it, Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        DelayedLoadingIndicator()
    }
}
