package com.muttaqi.android.designsystem.oneui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backIcon: Int = R.drawable.ic_arrow_back,
    backDescription: String = "Back",
    subtitle: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    expandable: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = OneUi.colors
    BoxWithConstraints(modifier) {
        val statusBar = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
        val wide = maxWidth >= 600.dp
        val canExpand = expandable && maxHeight >= 500.dp && maxHeight > maxWidth * 0.8f
        val proportion = if (wide) OneUiDefaults.EXPANDED_PROPORTION_WIDE else OneUiDefaults.EXPANDED_PROPORTION
        val minExpanded = (maxHeight * proportion - statusBar).coerceAtLeast(OneUiDefaults.CollapsedBarHeight)
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
            snapAnimationSpec = tween(300, easing = OneUiDefaults.Easing),
        )
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = colors.background,
            contentColor = colors.text,
            topBar = {
                OneUiAppBar(
                    title = title,
                    scrollBehavior = scrollBehavior,
                    expandable = canExpand,
                    minExpanded = minExpanded,
                    onBack = onBack,
                    backIcon = backIcon,
                    backDescription = backDescription,
                    subtitle = subtitle,
                    actions = actions,
                )
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OneUiAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    expandable: Boolean,
    minExpanded: Dp,
    onBack: (() -> Unit)?,
    backIcon: Int,
    backDescription: String,
    subtitle: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
) {
    val state = scrollBehavior.state
    val colors = OneUi.colors
    val type = OneUi.typography
    val fraction = { if (expandable) state.collapsedFraction else 1f }
    Layout(
        modifier = Modifier.fillMaxWidth().background(colors.background).windowInsetsPadding(WindowInsets.statusBars).clipToBounds(),
        content = {
            Column(
                Modifier.layoutId(HEADER).fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    title,
                    Modifier.semantics { heading() },
                    style = type.largeTitle.copy(color = colors.text, textAlign = TextAlign.Center),
                    maxLines = 3,
                    autoSize = TextAutoSize.StepBased(minFontSize = 22.sp, maxFontSize = type.largeTitle.fontSize, stepSize = 1.sp),
                )
                subtitle?.invoke()
            }
            Row(
                Modifier.layoutId(ROW).fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    BarIconButton(backIcon, backDescription, onBack)
                } else {
                    Spacer(Modifier.width(16.dp))
                }
                Text(
                    title,
                    Modifier.weight(1f).padding(horizontal = 4.dp).graphicsLayer { alpha = ((fraction() - 0.5f) * 2f).coerceIn(0f, 1f) },
                    style = type.barTitle,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                actions()
            }
        },
    ) { measurables, constraints ->
        val rowHeight = OneUiDefaults.CollapsedBarHeight.roundToPx()
        val width = constraints.maxWidth
        val row = measurables.first { it.layoutId == ROW }.measure(Constraints.fixed(width, rowHeight))
        if (!expandable) {
            if (state.heightOffsetLimit != 0f) state.heightOffsetLimit = 0f
            return@Layout layout(width, rowHeight) { row.place(0, 0) }
        }
        val header = measurables.first { it.layoutId == HEADER }.measure(Constraints(minWidth = width, maxWidth = width))
        val expanded = maxOf(minExpanded.roundToPx(), header.height + rowHeight)
        val limit = -(expanded - rowHeight).toFloat()
        if (state.heightOffsetLimit != limit) state.heightOffsetLimit = limit
        val height = (expanded + state.heightOffset).roundToInt().coerceIn(rowHeight, expanded)
        layout(width, height) {
            val area = height - rowHeight
            header.placeWithLayer(0, (area - header.height) / 2) {
                alpha = (1f - state.collapsedFraction * 1.6f).coerceIn(0f, 1f)
            }
            row.place(0, area)
        }
    }
}

@Composable
fun BarIconButton(icon: Int, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = description, Modifier.size(24.dp), tint = OneUi.colors.text)
    }
}

private const val HEADER = "header"
private const val ROW = "row"
