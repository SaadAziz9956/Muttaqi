package com.muttaqi.android.feature.topics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftTopBar
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

/** The emotions as tiles under the title and its verse; each opens its topic page */
@Composable
fun EmotionsScreen(state: EmotionsState, onIntent: (EmotionsIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val grid = rememberLazyGridState()
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SoftTopBar("Emotions", showTitle = grid.firstVisibleItemIndex > 0, onBack = onBack) },
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = grid,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 12.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) { PageHeader("Emotions", state.header, Modifier.padding(bottom = 22.dp)) }
                items(state.emotions, key = { it.id }) { emotion ->
                    SoftCard(cornerRadius = 22.dp, onClick = { onIntent(EmotionsIntent.EmotionTapped(emotion.id)) }) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                emotion.title,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                color = soft.appPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painterResource(R.drawable.ic_arrow_right_01_linear),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).rotate(-45f),
                                tint = soft.brandTeal,
                            )
                        }
                    }
                }
            }
        }
    }
}
