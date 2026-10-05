package com.muttaqi.android.feature.dhikr

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrCounterScreen(state: DhikrCounterState, onIntent: (DhikrCounterIntent) -> Unit, onBack: () -> Unit) {
    var confirmingReset by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val dhikr = state.dhikr
    val list = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBack) },
                actions = {
                    IconButton(onClick = { onIntent(DhikrCounterIntent.ShareTapped) }) {
                        Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
                    }
                    IconButton(onClick = { confirmingReset = true }, enabled = state.hasProgress) {
                        Icon(painterResource(R.drawable.ic_restart_alt), contentDescription = "Reset count")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (dhikr != null) {
                Column {
                    if (list.canScrollForward) HorizontalDivider()
                    Counter(dhikr, state, onCount = { onIntent(DhikrCounterIntent.Counted) })
                }
            }
        },
    ) { padding ->
        if (dhikr != null) {
            DhikrText(dhikr, state, list, Modifier.fillMaxSize().padding(padding))
        }
    }
    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text("Reset today's count?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmingReset = false
                    onIntent(DhikrCounterIntent.ResetConfirmed)
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmingReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DhikrText(dhikr: Dhikr, state: DhikrCounterState, list: LazyListState, modifier: Modifier = Modifier) {
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
        modifier,
        state = list,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        dhikr.title?.let { title ->
            item(key = "title") {
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
        }
        if (dhikr.steps.isEmpty()) {
            item(key = "phrase") { Phrase(dhikr) }
        } else {
            itemsIndexed(dhikr.steps, key = { index, _ -> "step-$index" }) { index, step ->
                val container by animateColorAsState(
                    when {
                        index == current -> MaterialTheme.colorScheme.primaryContainer
                        index < current -> MaterialTheme.colorScheme.surfaceContainerLow
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "step",
                )
                val content by animateColorAsState(
                    when {
                        index == current -> MaterialTheme.colorScheme.onPrimaryContainer
                        index < current -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "stepText",
                )
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${step.count}×", style = MaterialTheme.typography.labelLarge)
                            ArabicText(step.arabic, Modifier.weight(1f))
                        }
                        step.transliteration?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        step.translation?.let { TranslationText(it, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
        dhikr.hadith?.let { hadith ->
            item(key = "hadith") {
                Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hadith", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        TranslationText(hadith, Modifier.fillMaxWidth())
                    }
                }
            }
        }
        item(key = "source") {
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dhikr.reference, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(dhikr.grade, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                dhikr.credit?.let {
                    Text("Translation: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Phrase(dhikr: Dhikr) {
    Card(Modifier.fillMaxWidth()) {
        SelectionContainer {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ArabicText(dhikr.arabic, Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium)
                dhikr.transliteration?.let {
                    Text(it, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                dhikr.translation?.let { TranslationText(it, Modifier.fillMaxWidth()) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Counter(dhikr: Dhikr, state: DhikrCounterState, onCount: () -> Unit) {
    val complete = state.isRoundComplete
    val progress by animateFloatAsState(state.roundProgress.toFloat(), MaterialTheme.motionScheme.fastSpatialSpec(), label = "round")
    val target = state.target
    val caption = when {
        target == null -> if (state.count == 0) "Tap to count" else "times"
        complete -> "Complete"
        else -> "of $target"
    }
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.currentStep?.let { step ->
            val phrase = dhikr.steps[step.index]
            Text(
                listOfNotNull(phrase.transliteration, "${step.said} of ${phrase.count}").joinToString(" · "),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Box(Modifier.size(CounterRingSize), contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize())
            Button(
                onClick = onCount,
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.ExtraLargeContainerHeight),
                modifier = Modifier
                    .size(ButtonDefaults.ExtraLargeContainerHeight)
                    .semantics {
                        contentDescription = "Count"
                        stateDescription = if (target == null) "${state.count}" else "${state.count} of $target"
                        onClick(label = "count one") { onCount(); true }
                    },
                colors = if (complete) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                contentPadding = PaddingValues(8.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = state.count,
                        transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                        label = "count",
                    ) { count ->
                        Text("$count", style = MaterialTheme.typography.displayMedium)
                    }
                    Text(caption, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
        }
        Text(
            when {
                state.rounds == 0 -> ""
                state.rounds == 1 -> "Completed once today"
                else -> "Completed ${state.rounds} times today"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private val CounterRingSize = 168.dp
