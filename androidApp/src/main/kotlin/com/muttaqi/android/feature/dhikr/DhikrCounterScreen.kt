package com.muttaqi.android.feature.dhikr

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.component.softFloat
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrMilestone
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
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SoftTopBar("", showTitle = false, onBack = onBack) {
                    SoftIconButton(R.drawable.ic_export_arrow_01_linear, "Share", { onIntent(DhikrCounterIntent.ShareTapped) }, size = 44.dp, iconSize = 22.dp)
                    Spacer(Modifier.width(8.dp))
                    SoftIconButton(
                        R.drawable.ic_refresh_left_linear,
                        "Reset count",
                        { if (state.hasProgress) confirmingReset = true },
                        Modifier.padding(end = 12.dp).alpha(if (state.hasProgress) 1f else 0.4f),
                        size = 44.dp,
                        iconSize = 22.dp,
                    )
                }
            },
        ) { padding ->
            val dhikr = state.dhikr
            if (dhikr != null) {
                Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                    DhikrText(dhikr, state)
                    Counter(dhikr, state, onCount = { onIntent(DhikrCounterIntent.Counted) }, modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text("Reset today's count?", style = MaterialTheme.typography.titleMedium) },
            confirmButton = {
                TextButton(onClick = {
                    confirmingReset = false
                    onIntent(DhikrCounterIntent.ResetConfirmed)
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmingReset = false }) { Text("Cancel") } },
            containerColor = MuttaqiTheme.soft.canvas,
        )
    }
}

@Composable
private fun DhikrText(dhikr: Dhikr, state: DhikrCounterState) {
    val soft = MuttaqiTheme.soft
    val list = rememberLazyListState()
    val current = state.currentStep?.index ?: 0
    val firstStep = if (dhikr.title != null) 1 else 0
    var shownStep by remember { mutableStateOf(current) }
    LaunchedEffect(current) {
        if (current == shownStep || dhikr.steps.isEmpty()) return@LaunchedEffect
        shownStep = current
        val viewport = list.layoutInfo.viewportSize.height
        val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == firstStep + current }?.size ?: 0
        list.animateScrollToItem(firstStep + current, scrollOffset = -(viewport - item) / 2)
    }
    LazyColumn(
        state = list,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 260.dp),
    ) {
        dhikr.title?.let { title ->
            item(key = "title") {
                Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = soft.brandTeal)
            }
        }
        if (dhikr.steps.isEmpty()) {
            item(key = "phrase") { Phrase(dhikr, Modifier.padding(top = 16.dp)) }
        } else {
            itemsIndexed(dhikr.steps, key = { index, _ -> "step-$index" }) { index, step ->
                val highlight by animateFloatAsState(if (index == current) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "current")
                val fade by animateFloatAsState(if (index < current) 0.45f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "said")
                val shape = RoundedCornerShape(22.dp)
                SoftCard(
                    Modifier
                        .padding(top = if (index == 0) 16.dp else 12.dp)
                        .graphicsLayer { alpha = fade }
                        .border(2.dp, soft.brandGreen.copy(alpha = highlight), shape),
                    cornerRadius = 22.dp,
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${step.count}×", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = soft.brandTeal)
                            Spacer(Modifier.width(12.dp))
                            ArabicText(step.arabic, Modifier.weight(1f), fontSize = 22.sp, textAlign = TextAlign.Right, lineSpacing = 0.sp)
                        }
                        step.transliteration?.let { Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = soft.appPrimary) }
                        step.translation?.let { Translated(it, 13, soft.textSecondary) }
                    }
                }
            }
        }
        dhikr.hadith?.let { hadith ->
            item(key = "hadith") {
                SoftCard(Modifier.padding(top = 28.dp), cornerRadius = 24.dp) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Hadith", style = MaterialTheme.typography.labelMedium, color = soft.brandTeal)
                        Translated(hadith, 15, soft.textPrimary)
                    }
                }
            }
        }
        item(key = "source") {
            Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dhikr.reference, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
                val small = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Default, fontSize = 11.sp)
                Text(dhikr.grade, style = small, color = soft.textSecondary)
                dhikr.credit?.let { Text("Translation: $it", style = small, color = soft.textSecondary) }
            }
        }
    }
}

@Composable
private fun Phrase(dhikr: Dhikr, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    SoftCard(modifier, cornerRadius = 26.dp) {
        SelectionContainer {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                ArabicText(dhikr.arabic, Modifier.fillMaxWidth(), fontSize = 28.sp, textAlign = TextAlign.Right, lineSpacing = 12.sp)
                dhikr.transliteration?.let { Text(it, Modifier.padding(top = 20.dp), style = MaterialTheme.typography.bodyMedium, color = soft.appPrimary) }
                dhikr.translation?.let { Box(Modifier.padding(top = 10.dp)) { Translated(it, 15, soft.textPrimary) } }
            }
        }
    }
}

@Composable
private fun Translated(text: String, size: Int, color: Color) {
    TranslationText(
        text,
        Modifier.fillMaxWidth(),
        fontSize = size.sp,
        color = color,
        textAlign = if (text.isArabicScript()) TextAlign.Right else TextAlign.Left,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Counter(dhikr: Dhikr, state: DhikrCounterState, onCount: () -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val complete = state.isRoundComplete
    val progress by animateFloatAsState(state.roundProgress.toFloat(), MaterialTheme.motionScheme.fastSpatialSpec(), label = "round")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), label = "press")
    val stroke = with(LocalDensity.current) { Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round) }
    val target = state.target
    val caption = when {
        target == null -> if (state.count == 0) "Tap to count" else "times"
        complete -> "Complete"
        else -> "of $target"
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to soft.canvas.copy(alpha = 0f), 0.3f to soft.canvas.copy(alpha = 0.92f), 1f to soft.canvas))
            .navigationBarsPadding()
            .padding(top = 44.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.currentStep?.let { step ->
            val phrase = dhikr.steps[step.index]
            Text(listOfNotNull(phrase.transliteration, "${step.said} of ${phrase.count}").joinToString(" · "), style = MaterialTheme.typography.labelLarge, color = soft.appPrimary)
        }
        Box(
            Modifier
                .size(164.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .softFloat(CircleShape, elevation = 10.dp)
                .clip(CircleShape)
                .background(if (complete) soft.brandGreen else soft.surface)
                .then(if (complete) Modifier else Modifier.border(1.5.dp, soft.rim, CircleShape))
                .clickable(interaction, indication = null, onClick = onCount)
                .semantics {
                    contentDescription = "Count"
                    stateDescription = if (target == null) "${state.count}" else "${state.count} of $target"
                    onClick(label = "count one") { onCount(); true }
                },
            contentAlignment = Alignment.Center,
        ) {
            CircularWavyProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize().padding(10.dp),
                color = soft.brandGreen,
                trackColor = soft.brandTeal.copy(alpha = 0.18f),
                stroke = stroke,
                trackStroke = stroke,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = state.count,
                    transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                    label = "count",
                ) { count ->
                    Text(
                        "$count",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp, fontWeight = FontWeight.Medium),
                        color = if (complete) Color.White else soft.appPrimary,
                    )
                }
                Text(caption, style = MaterialTheme.typography.labelSmall, color = if (complete) Color.White.copy(alpha = 0.85f) else soft.textSecondary)
            }
        }
        Text(
            if (state.rounds == 1) "Completed once today" else "Completed ${state.rounds} times today",
            Modifier.alpha(if (state.rounds > 0) 1f else 0f),
            style = MaterialTheme.typography.labelSmall,
            color = soft.brandTeal,
        )
    }
}
