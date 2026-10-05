package com.muttaqi.android.feature.names

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.names.presentation.NamesEffect
import com.muttaqi.shared.feature.names.presentation.NamesIntent
import com.muttaqi.shared.feature.names.presentation.NamesState
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import kotlin.math.max
import kotlinx.coroutines.flow.drop

@Composable
fun NamesRoute(viewModel: NamesViewModel, onOpenSearch: () -> Unit, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NamesEffect.OpenShare -> onShare(effect.passage)
                is NamesEffect.ShowName -> Unit
            }
        }
    }
    val pager = rememberPagerState(initialPage = state.names.indexOfFirst { it.number == position }.coerceAtLeast(0)) { state.names.size }
    NamesScreen(state, pager, position, viewModel::dispatch, onOpenSearch, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesScreen(
    state: NamesState,
    pager: PagerState,
    position: Int,
    onIntent: (NamesIntent) -> Unit,
    onOpenSearch: () -> Unit,
    onBack: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(pager, state.names) {
        snapshotFlow { pager.settledPage }.collect { page -> state.names.getOrNull(page)?.let { onIntent(NamesIntent.NameShown(it.number)) } }
    }
    LaunchedEffect(position, state.names) {
        val index = state.names.indexOfFirst { it.number == position }
        if (index >= 0 && index != pager.settledPage) pager.scrollToPage(index)
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.drop(1).collect { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    }
    val shownNumber = state.names.getOrNull(pager.currentPage)?.number ?: position

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("99 Names") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (state.names.isNotEmpty()) {
                        IconButton(onClick = { onIntent(NamesIntent.ShareTapped) }) {
                            Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
                        }
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(painterResource(R.drawable.ic_search), contentDescription = "Search names")
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding)) {
            val cardHeight = max(260f, maxHeight.value * 0.55f).dp
            Column(
                Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.padding(top = 16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp,
                        beyondViewportPageCount = 1,
                        key = { page -> state.names.getOrNull(page)?.number ?: -(page + 1) },
                    ) { page ->
                        val name = state.names.getOrNull(page) ?: return@HorizontalPager
                        NameCard(name, minHeight = cardHeight)
                    }
                    AnimatedContent(
                        targetState = shownNumber,
                        modifier = Modifier.padding(top = 16.dp),
                        transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                        label = "position",
                    ) { number ->
                        Text(
                            "$number of ${max(state.names.size, 99)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                state.hadith?.let { hadith ->
                    Card(Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TranslationText(hadith.text, Modifier.fillMaxWidth(), lineSpacing = 0.sp)
                            Text(hadith.source, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(state.listSource, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
