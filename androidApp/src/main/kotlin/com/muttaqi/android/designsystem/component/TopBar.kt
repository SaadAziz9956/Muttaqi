package com.muttaqi.android.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftTopBar(
    title: String,
    showTitle: Boolean,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val canvas = MuttaqiTheme.soft.canvas
    val edge by animateFloatAsState(if (showTitle) 1f else 0f, label = "edge")
    CenterAlignedTopAppBar(
        modifier = modifier.drawBehind { if (edge > 0f) drawEdgeFade(canvas, solidHeight = size.height, alpha = edge) },
        title = {
            AnimatedVisibility(showTitle, enter = fadeIn(), exit = fadeOut()) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MuttaqiTheme.soft.appPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (onBack != null) {
                SoftIconButton(R.drawable.ic_arrow_left_02_linear, "Back", onBack, Modifier.padding(start = 12.dp), size = 44.dp, iconSize = 22.dp)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
    )
}

@Composable
fun StatusBarFade(visible: Boolean, modifier: Modifier = Modifier) {
    val canvas = MuttaqiTheme.soft.canvas
    val edge by animateFloatAsState(if (visible) 1f else 0f, label = "edge")
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Spacer(
        modifier
            .fillMaxWidth()
            .height(statusBar + EdgeFade)
            .drawBehind { if (edge > 0f) drawEdgeFade(canvas, solidHeight = size.height - EdgeFade.toPx(), alpha = edge) },
    )
}

private fun DrawScope.drawEdgeFade(canvas: Color, solidHeight: Float, alpha: Float) {
    val height = solidHeight + EdgeFade.toPx()
    drawRect(
        Brush.verticalGradient(
            0f to canvas,
            solidHeight / height to canvas.copy(alpha = 0.85f),
            1f to canvas.copy(alpha = 0f),
            endY = height,
        ),
        size = Size(size.width, height),
        alpha = alpha,
    )
}

private val EdgeFade = 28.dp
