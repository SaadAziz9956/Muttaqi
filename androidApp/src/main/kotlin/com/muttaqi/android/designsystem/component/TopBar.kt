package com.muttaqi.android.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme

/**
 * The bar over a page: a round back button, and the page's title once its own large title has scrolled away (like
 * iOS's collapsing titles). [showTitle] is usually `listState.firstVisibleItemIndex > 0`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftTopBar(
    title: String,
    showTitle: Boolean,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
    )
}
