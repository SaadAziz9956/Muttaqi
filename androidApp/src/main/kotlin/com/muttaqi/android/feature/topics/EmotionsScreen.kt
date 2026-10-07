package com.muttaqi.android.feature.topics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUiListRow
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsEffect
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsIntent
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsState
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EmotionsRoute(onOpenEmotion: (String) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<EmotionsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EmotionsEffect.OpenEmotion -> onOpenEmotion(effect.emotionId)
            }
        }
    }
    EmotionsScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun EmotionsScreen(state: EmotionsState, onIntent: (EmotionsIntent) -> Unit, onBack: () -> Unit) {
    OneUiSurface {
        OneUiScaffold(
            title = "Emotions",
            onBack = onBack,
            subtitle = state.header?.let { header -> { OneUiHeaderQuote(header.text, header.source) } },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                if (state.emotions.isNotEmpty()) {
                    item(key = "emotions") {
                        OneUiGroup {
                            state.emotions.forEachIndexed { index, emotion ->
                                OneUiListRow(
                                    title = emotion.title,
                                    summary = passageSummary(emotion).ifEmpty { null },
                                    divider = index < state.emotions.lastIndex,
                                    onClick = { onIntent(EmotionsIntent.EmotionTapped(emotion.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
