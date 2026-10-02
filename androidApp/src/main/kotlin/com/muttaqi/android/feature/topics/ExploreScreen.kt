package com.muttaqi.android.feature.topics

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftSearchField
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreEffect
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreIntent
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreState
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExploreRoute(onOpenTopic: (String) -> Unit) {
    val viewModel = koinViewModel<ExploreViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExploreEffect.OpenTopic -> onOpenTopic(effect.topicId)
            }
        }
    }
    ExploreScreen(state, viewModel::dispatch)
}

@Composable
fun ExploreScreen(state: ExploreState, onIntent: (ExploreIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    Box {
        SoftBackdrop()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { PageHeader("Explore", state.header) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SoftSearchField(
                    query = state.query,
                    onQueryChange = { onIntent(ExploreIntent.QueryChanged(it)) },
                    onClear = { onIntent(ExploreIntent.ClearQuery) },
                    modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
                )
            }
            if (state.isSearching) {
                if (state.results.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NoResults(state.query) }
                }
                items(state.results, key = { it.topic.id }, span = { GridItemSpan(maxLineSpan) }) { result ->
                    SoftCard(cornerRadius = 22.dp, onClick = { onIntent(ExploreIntent.TopicTapped(result.topic.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(topicIcon(result.topic.icon)), null, Modifier.size(22.dp), tint = soft.brandTeal)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(result.topic.title, style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary)
                                Text(result.group.title, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
                            }
                            Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.size(16.dp), tint = soft.textSecondary)
                        }
                    }
                }
            } else {
                state.groups.forEach { group ->
                    item(key = group.id, span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            group.title,
                            Modifier.padding(top = 14.dp).semantics { heading() },
                            style = MaterialTheme.typography.titleSmall,
                            color = soft.appPrimary,
                        )
                    }
                    items(group.topics, key = { it.id }) { topic ->
                        TopicTile(topic, onClick = { onIntent(ExploreIntent.TopicTapped(topic.id)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicTile(topic: ExploreTopic, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftCard(cornerRadius = 22.dp, onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.background(soft.tintedSurface, CircleShape).padding(8.dp)) {
                Icon(painterResource(topicIcon(topic.icon)), null, Modifier.size(18.dp), tint = soft.brandTeal)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                topic.title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                color = soft.appPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NoResults(query: String) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_search_normal_linear), null, Modifier.size(44.dp), tint = soft.textSecondary)
        Text(
            "No Results for “$query”",
            Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = soft.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            "Check the spelling or try a new search.",
            Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = soft.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@SuppressLint("DiscouragedApi")
@DrawableRes
@Composable
internal fun topicIcon(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier("ic_" + name.replace('-', '_'), "drawable", context.packageName)
            .takeIf { it != 0 } ?: R.drawable.ic_book_open_linear
    }
}
