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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiArtwork
import com.muttaqi.android.designsystem.oneui.OneUiBottomSheet
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
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
        OneUiBottomSheet(onDismiss = { showSettings = false }) {
            ReadingSettingsContent(settingsState, settings::dispatch)
        }
    }
    tafsirStart?.let { start ->
        OneUiBottomSheet(
            onDismiss = { tafsirStart = null },
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
    OneUiSurface {
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
}

@Composable
private fun SurahPage(state: SurahReaderState, onIntent: (SurahReaderIntent) -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    val list = rememberLazyListState()
    val header = state.headerSurah
    val collapser = remember { NestedScrollDispatcher() }
    TrackVisibleAyahs(list, onIntent)
    ScrollToStart(state, list, collapser, onIntent)
    OneUiScaffold(
        title = header?.englishName.orEmpty(),
        onBack = onBack,
        subtitle = header?.let { surah -> { SurahFacts(surah) } },
        actions = { OneUiIconButton(R.drawable.ic_tune, "Reading settings", onSettings) },
    ) { padding ->
        LazyColumn(
            Modifier.nestedScroll(PassThrough, collapser),
            state = list,
            contentPadding = PaddingValues(
                start = OneUiDefaults.ScreenMargin,
                end = OneUiDefaults.ScreenMargin,
                top = padding.calculateTopPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing),
        ) {
            item(key = "header") {
                SurahHeader(state.previousSurah, state.nextSurah, onIntent)
            }
            when (val content = state.content) {
                SurahReaderContent.Loading -> item(key = "loading") { Loading(Modifier.padding(top = 64.dp)) }
                is SurahReaderContent.Failed -> item(key = "failed") {
                    LoadFailed(
                        content.message,
                        onRetry = { onIntent(SurahReaderIntent.Retry) },
                        modifier = Modifier.padding(top = 24.dp).height(360.dp),
                        suggestion = content.suggestion,
                    )
                }
                is SurahReaderContent.Loaded -> loadedContent(content.reading, state, onIntent)
            }
        }
    }
}

private val PassThrough = object : NestedScrollConnection {}

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
            item(key = "pages-top") { Spacer(Modifier.height(0.dp)) }
            items(reading.pages, key = { it.id }) { page -> Arriving { MushafPageCard(page, state.settings.fontSize) } }
        }
    }
    item(key = "end") {
        Arriving {
            Column(
                Modifier.navigationBarsPadding().padding(top = 8.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(OneUiDefaults.GroupGap),
            ) {
                SurahEndNavigation(reading.previousSurah, reading.nextSurah, onIntent)
                Text(
                    QuranMessages.MUSHAF_CREDIT + "\n" + QuranMessages.translationCredit(state.settings.language),
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    style = OneUi.typography.small,
                    color = OneUi.colors.secondaryText,
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
private fun ScrollToStart(state: SurahReaderState, list: LazyListState, collapser: NestedScrollDispatcher, onIntent: (SurahReaderIntent) -> Unit) {
    val target = state.startScrollTarget
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        repeat(2) {
            collapser.dispatchPreScroll(Offset(0f, -COLLAPSE_DISTANCE), NestedScrollSource.SideEffect)
            list.scrollToItem(itemIndex(state, target))
            delay(300)
        }
        onIntent(SurahReaderIntent.ReachedStart)
    }
}

private const val COLLAPSE_DISTANCE = 100_000f

private fun itemIndex(state: SurahReaderState, target: Int): Int {
    val reading = state.reading ?: return 0
    val leading = 1 + (if (reading.showsBismillah) 1 else 0)
    return when (state.settings.mode) {
        ReadingMode.WithTranslation -> leading + reading.ayahs.indexOfFirst { it.number == target }.coerceAtLeast(0)
        ReadingMode.ArabicOnly -> leading + 1 + reading.pages.indexOfFirst { it.id == target }.coerceAtLeast(0)
    }
}

@Composable
private fun SurahFacts(surah: Surah) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        ArabicText(
            surah.name,
            Modifier.fillMaxWidth(),
            style = type.sectionTitle.copy(fontSize = 26.sp),
            color = colors.accent,
            textAlign = TextAlign.Center,
        )
        Text(
            "${surah.englishNameTranslation} · ${surah.revelationType} · ${surah.numberOfAyahs} ayahs",
            style = type.listSummary,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SurahHeader(previous: Surah?, next: Surah?, onIntent: (SurahReaderIntent) -> Unit) {
    OneUiCard(
        Modifier.fillMaxWidth(),
        artwork = OneUiArtwork.Dawn,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 20.dp, bottom = if (previous != null || next != null) 8.dp else 20.dp),
    ) {
        OneUiButton(
            "Explanation",
            onClick = { onIntent(SurahReaderIntent.ExplanationTapped) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            icon = R.drawable.ic_book,
        )
        if (previous != null || next != null) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                previous?.let { NeighbourLink(it, isNext = false) { onIntent(SurahReaderIntent.PreviousTapped) } }
                Spacer(Modifier.weight(1f))
                next?.let { NeighbourLink(it, isNext = true) { onIntent(SurahReaderIntent.NextTapped) } }
            }
        }
    }
}

@Composable
private fun NeighbourLink(neighbour: Surah, isNext: Boolean, onClick: () -> Unit) {
    val color = LocalContentColor.current
    Row(
        Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isNext) Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, Modifier.size(18.dp), tint = color)
        Text(neighbour.englishName, style = OneUi.typography.listSummary.copy(fontWeight = FontWeight.Medium), color = color)
        if (isNext) Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, Modifier.size(18.dp), tint = color)
    }
}

@Composable
private fun Bismillah(text: String, translation: String?) {
    val colors = OneUi.colors
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = OneUiDefaults.ItemPadding, vertical = 24.dp)) {
        QuranText(text, Modifier.fillMaxWidth(), color = colors.text, lineSpacing = 0.sp)
        translation?.let {
            TranslationText(
                it,
                Modifier.fillMaxWidth().padding(top = 12.dp),
                style = OneUi.typography.listSummary,
                color = colors.secondaryText,
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
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OneUiDefaults.ContainerRadius))
            .background(colors.container)
            .padding(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 18.dp, bottom = 8.dp),
    ) {
        AyahArabic(ayah, fontSize)
        ayah.transliteration?.takeIf { it.isNotEmpty() }?.let {
            TranslationText(
                it,
                Modifier.fillMaxWidth().padding(top = 12.dp),
                style = type.body,
                fontSize = fontSize.transliterationSize.sp,
                color = colors.accent,
                textAlign = TextAlign.Left,
                lineSpacing = 0.sp,
            )
        }
        ayah.translation?.takeIf { it.isNotEmpty() }?.let { translation ->
            if (language == Language.Urdu) {
                TranslationText(
                    translation,
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    style = type.body,
                    fontSize = (fontSize.translationSize - 1).sp,
                    color = colors.text,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
            } else {
                TranslationText(
                    "${ayah.numberInSurah}.  $translation",
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    style = type.body,
                    fontSize = fontSize.translationSize.sp,
                    color = colors.text,
                    textAlign = TextAlign.Left,
                    lineSpacing = 0.sp,
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                ayah.reference,
                Modifier.weight(1f),
                style = type.caption.copy(fontWeight = FontWeight.Medium),
                color = colors.accent,
            )
            AyahAction(R.drawable.ic_book, "Explanation", onExplanation)
            AyahAction(
                if (copied) R.drawable.ic_check else R.drawable.ic_content_copy,
                if (copied) "Copied" else "Copy",
            ) {
                onCopy()
                copied = true
            }
            AyahAction(R.drawable.ic_share, "Share", onShare)
        }
    }
}

@Composable
private fun AyahAction(icon: Int, description: String, onClick: () -> Unit) {
    OneUiIconButton(icon, description, onClick, tint = OneUi.colors.secondaryText, size = 40.dp, iconSize = 20.dp)
}

@Composable
private fun AyahArabic(ayah: Ayah, fontSize: FontSize) {
    val colors = OneUi.colors
    val markColor = colors.accent
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
        color = colors.text,
        textAlign = TextAlign.Right,
        style = TextStyle(fontFamily = QuranFont, fontSize = fontSize.arabicSize.sp, textDirection = TextDirection.Rtl),
    )
}

@Composable
fun MushafPageCard(page: MushafPage, fontSize: FontSize) {
    val colors = OneUi.colors
    val markColor = colors.accent
    val text = remember(page.id, markColor) {
        buildAnnotatedString {
            page.ayahs.forEachIndexed { index, ayah ->
                append(ayah.arabicWithoutEndSign())
                append(' ')
                withStyle(SpanStyle(color = markColor)) { append(ayah.ayahMark) }
                if (index < page.ayahs.lastIndex) append(' ')
            }
        }
    }
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 20.dp, bottom = 16.dp)) {
        Text(
            text,
            Modifier.fillMaxWidth(),
            color = colors.text,
            style = TextStyle(
                fontFamily = QuranFont,
                fontSize = fontSize.arabicSize.sp,
                lineHeight = (QURAN_FONT_LINE_HEIGHT + MUSHAF_LINE_SPACING).em,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both),
                textAlign = TextAlign.Justify,
                textDirection = TextDirection.Rtl,
            ),
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(colors.divider))
            Text("${page.number}", style = OneUi.typography.caption, color = colors.secondaryText)
            Box(Modifier.weight(1f).height(1.dp).background(colors.divider))
        }
    }
}

private const val QURAN_FONT_LINE_HEIGHT = 1.758f
private const val MUSHAF_LINE_SPACING = 0.6f

@Composable
private fun SurahEndNavigation(previous: Surah?, next: Surah?, onIntent: (SurahReaderIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
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
    val colors = OneUi.colors
    val type = OneUi.typography
    OneUiCard(
        Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = OneUiDefaults.ItemPadding, vertical = 18.dp),
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = if (isNext) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(if (isNext) "Next" else "Previous", style = type.caption, color = colors.secondaryText)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isNext) Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, Modifier.size(18.dp), tint = colors.accent)
                Text(neighbour.englishName, Modifier.weight(1f, fill = false), style = type.listTitle, color = colors.text)
                if (isNext) Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, Modifier.size(18.dp), tint = colors.accent)
            }
        }
    }
}

@Composable
private fun ReadingPosition(position: String) {
    val colors = OneUi.colors
    Box(
        Modifier
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.tabBar.copy(alpha = 1f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        AnimatedContent(position, label = "position") {
            Text(it, style = OneUi.typography.listSummary.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
        }
    }
}

@Composable
internal fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        DelayedLoadingIndicator()
    }
}
