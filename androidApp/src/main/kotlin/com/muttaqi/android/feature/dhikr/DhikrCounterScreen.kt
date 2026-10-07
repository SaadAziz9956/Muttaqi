package com.muttaqi.android.feature.dhikr

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiCountPill
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiDialog
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrMilestone
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStep
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterEffect
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterIntent
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterState
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DhikrCounterRoute(dhikrId: String, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DhikrCounterViewModel> { parametersOf(dhikrId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DhikrCounterEffect.Counted -> haptics.performHapticFeedback(
                    when (effect.milestone) {
                        DhikrMilestone.Repetition -> HapticFeedbackType.SegmentTick
                        DhikrMilestone.PhraseFinished -> HapticFeedbackType.GestureThresholdActivate
                        DhikrMilestone.RoundFinished -> HapticFeedbackType.Confirm
                    },
                )
                is DhikrCounterEffect.OpenShare -> onShare(effect.passage)
            }
        }
    }
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    DhikrCounterScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun DhikrCounterScreen(state: DhikrCounterState, onIntent: (DhikrCounterIntent) -> Unit, onBack: () -> Unit) {
    var confirmingReset by rememberSaveable { mutableStateOf(false) }
    val dhikr = state.dhikr
    val list = rememberLazyListState()
    OneUiSurface {
        OneUiScaffold(
            title = dhikr?.let { it.title ?: FALLBACK_TITLE }.orEmpty(),
            onBack = onBack,
            actions = {
                OneUiIconButton(R.drawable.ic_share, "Share", onClick = { onIntent(DhikrCounterIntent.ShareTapped) })
                OneUiIconButton(R.drawable.ic_restart_alt, "Reset count", onClick = { confirmingReset = true }, enabled = state.hasProgress)
            },
            bottomBar = {
                if (dhikr != null) {
                    Column(Modifier.fillMaxWidth().background(OneUi.colors.background)) {
                        if (list.canScrollForward) Box(Modifier.fillMaxWidth().height(1.dp).background(OneUi.colors.divider))
                        Counter(dhikr, state, onCount = { onIntent(DhikrCounterIntent.Counted) })
                    }
                }
            },
        ) { padding ->
            if (dhikr != null) {
                DhikrText(dhikr, state, list, padding)
            }
        }
        if (confirmingReset) {
            OneUiDialog(
                title = "Reset today's count?",
                onDismiss = { confirmingReset = false },
                confirmText = "Reset",
                onConfirm = {
                    confirmingReset = false
                    onIntent(DhikrCounterIntent.ResetConfirmed)
                },
                destructive = true,
            )
        }
    }
}

@Composable
private fun DhikrText(dhikr: Dhikr, state: DhikrCounterState, list: LazyListState, padding: PaddingValues) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val current = state.currentStep?.index ?: 0
    var shownStep by remember { mutableStateOf(current) }
    LaunchedEffect(current) {
        if (current == shownStep || dhikr.steps.isEmpty()) return@LaunchedEffect
        shownStep = current
        val viewport = list.layoutInfo.viewportSize.height
        val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == current }?.size ?: 0
        list.animateScrollToItem(current, scrollOffset = -(viewport - item) / 2)
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        state = list,
        contentPadding = PaddingValues(
            start = OneUiDefaults.ScreenMargin,
            end = OneUiDefaults.ScreenMargin,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing),
    ) {
        if (dhikr.steps.isEmpty()) {
            item(key = "phrase") { Phrase(dhikr) }
        } else {
            itemsIndexed(dhikr.steps, key = { index, _ -> "step-$index" }) { index, step ->
                StepCard(step, isCurrent = index == current, isDone = index < current)
            }
        }
        dhikr.hadith?.let { hadith ->
            item(key = "hadith") {
                OneUiCard(Modifier.fillMaxWidth().padding(top = OneUiDefaults.GroupGap - OneUiCardSpacing)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hadith", style = type.subheader, color = colors.accent)
                        TranslationText(hadith, Modifier.fillMaxWidth(), style = type.body, color = colors.text)
                    }
                }
            }
        }
        item(key = "source") {
            Column(
                Modifier.fillMaxWidth().padding(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(dhikr.reference, style = type.caption.copy(fontWeight = FontWeight.Medium), color = colors.accent)
                Text(dhikr.grade, style = type.small, color = colors.secondaryText)
                dhikr.credit?.let {
                    Text("Translation: $it", style = type.small, color = colors.secondaryText)
                }
            }
        }
    }
}

@Composable
private fun StepCard(step: DhikrStep, isCurrent: Boolean, isDone: Boolean) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val outline by animateColorAsState(if (isCurrent) colors.accent else Color.Transparent, tween(250, easing = OneUiDefaults.Easing), label = "stepOutline")
    val content by animateColorAsState(if (isDone) colors.secondaryText else colors.text, tween(250, easing = OneUiDefaults.Easing), label = "stepText")
    val shape = RoundedCornerShape(OneUiDefaults.ContainerRadius)
    OneUiCard(Modifier.fillMaxWidth().border(2.dp, outline, shape), shape = shape) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OneUiCountPill("${step.count}×")
                ArabicText(step.arabic, Modifier.weight(1f), color = content)
            }
            step.transliteration?.let {
                Text(it, style = type.listSummary, color = if (isDone) colors.secondaryText else colors.accent)
            }
            step.translation?.let { TranslationText(it, Modifier.fillMaxWidth(), style = type.listSummary, color = content) }
        }
    }
}

@Composable
private fun Phrase(dhikr: Dhikr) {
    val colors = OneUi.colors
    val type = OneUi.typography
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 24.dp, bottom = 22.dp)) {
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ArabicText(dhikr.arabic, Modifier.fillMaxWidth(), style = type.largeTitle.copy(fontSize = 28.sp), color = colors.text)
                dhikr.transliteration?.let {
                    Text(it, Modifier.padding(top = 4.dp), style = type.body, color = colors.accent)
                }
                dhikr.translation?.let { TranslationText(it, Modifier.fillMaxWidth(), style = type.body, color = colors.text) }
            }
        }
    }
}

@Composable
private fun Counter(dhikr: Dhikr, state: DhikrCounterState, onCount: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val complete = state.isRoundComplete
    val progress by animateFloatAsState(state.roundProgress.toFloat(), tween(300, easing = OneUiDefaults.Easing), label = "round")
    val fill by animateColorAsState(if (complete) colors.accent else colors.container, tween(250, easing = OneUiDefaults.Easing), label = "counterFill")
    val onFill by animateColorAsState(if (complete) colors.onAccent else colors.text, tween(250, easing = OneUiDefaults.Easing), label = "counterText")
    val target = state.target
    val caption = when {
        target == null -> if (state.count == 0) "Tap to count" else "times"
        complete -> "Complete"
        else -> "of $target"
    }
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        state.currentStep?.let { step ->
            val phrase = dhikr.steps[step.index]
            Text(
                listOfNotNull(phrase.transliteration, "${step.said} of ${phrase.count}").joinToString(" · "),
                style = type.listSummary.copy(fontWeight = FontWeight.Medium),
                color = colors.accent,
                textAlign = TextAlign.Center,
            )
        }
        Box(Modifier.size(CounterRingSize), contentAlignment = Alignment.Center) {
            ProgressRing(
                progress = { progress },
                modifier = Modifier.fillMaxSize().semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
            )
            Box(
                Modifier
                    .size(CounterButtonSize)
                    .clip(CircleShape)
                    .background(fill)
                    .clickable(role = Role.Button, onClick = onCount)
                    .semantics {
                        contentDescription = "Count"
                        stateDescription = if (target == null) "${state.count}" else "${state.count} of $target"
                        onClick(label = "count one") { onCount(); true }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = state.count,
                        transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                        label = "count",
                    ) { count ->
                        Text("$count", style = type.largeTitle.copy(fontSize = 46.sp, lineHeight = 54.sp), color = onFill)
                    }
                    Text(caption, style = type.caption, color = if (complete) onFill else colors.secondaryText, textAlign = TextAlign.Center)
                }
            }
        }
        Text(
            when {
                state.rounds == 0 -> ""
                state.rounds == 1 -> "Completed once today"
                else -> "Completed ${state.rounds} times today"
            },
            style = type.caption.copy(fontWeight = FontWeight.Medium),
            color = colors.accent,
        )
    }
}

@Composable
private fun ProgressRing(progress: () -> Float, modifier: Modifier = Modifier) {
    val track = OneUi.colors.component
    val accent = OneUi.colors.accent
    Canvas(modifier) {
        val stroke = RingStroke.toPx()
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arc = Size(size.width - stroke, size.height - stroke)
        drawArc(track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arc, style = Stroke(stroke))
        val sweep = progress().coerceIn(0f, 1f) * 360f
        if (sweep > 0f) {
            drawArc(accent, startAngle = -90f, sweepAngle = sweep, useCenter = false, topLeft = topLeft, size = arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

private val CounterRingSize = 184.dp
private val CounterButtonSize = 152.dp
private val RingStroke = 6.dp
private const val FALLBACK_TITLE = "Zikr"
