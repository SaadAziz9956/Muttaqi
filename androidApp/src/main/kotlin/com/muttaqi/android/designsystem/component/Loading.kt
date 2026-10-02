package com.muttaqi.android.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.muttaqi.android.designsystem.MuttaqiTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftLoadingIndicator(modifier: Modifier = Modifier, after: Duration = 250.milliseconds) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(after)
        shown = true
    }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, tween(220), label = "loading")
    LoadingIndicator(modifier.graphicsLayer { this.alpha = alpha }, color = MuttaqiTheme.soft.appPrimary)
}

@Composable
fun <T> FadeBetween(
    target: T,
    key: (T) -> Any?,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = target,
        modifier = modifier,
        transitionSpec = { fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120)) },
        contentAlignment = contentAlignment,
        contentKey = key,
        label = "fadeBetween",
    ) { content(it) }
}
