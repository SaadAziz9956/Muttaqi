package com.muttaqi.android.feature.names

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.names.presentation.NamesEffect
import com.muttaqi.shared.feature.names.presentation.NamesIntent
import com.muttaqi.shared.feature.names.presentation.NamesState
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlinx.coroutines.flow.drop

/** The 99 Names page; [viewModel] is shared with its search, so a picked result turns the page */
@Composable
fun NamesRoute(viewModel: NamesViewModel, onOpenSearch: () -> Unit, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NamesEffect.OpenShare -> onShare(effect.passage)
                // The page follows the position, which the pick has already moved
                is NamesEffect.ShowName -> Unit
            }
        }
    }
    val pager = rememberPagerState(initialPage = state.names.indexOfFirst { it.number == position }.coerceAtLeast(0)) { state.names.size }
    NamesScreen(state, pager, position, viewModel::dispatch, onOpenSearch, onBack)
}

/**
 * One name at a time, with the next and previous peeking in at the edges, smaller and faded, which of the 99 is
 * showing, and the hadith at the foot of the page
 */
@Composable
fun NamesScreen(
    state: NamesState,
    pager: PagerState,
    position: Int,
    onIntent: (NamesIntent) -> Unit,
    onOpenSearch: () -> Unit,
    onBack: () -> Unit,
) {
    val soft = MuttaqiTheme.soft
    val haptics = LocalHapticFeedback.current
    // The page reports where it settles, and follows the position when it moves elsewhere, e.g. to a search pick
    LaunchedEffect(pager, state.names) {
        snapshotFlow { pager.settledPage }.collect { page -> state.names.getOrNull(page)?.let { onIntent(NamesIntent.NameShown(it.number)) } }
    }
    LaunchedEffect(position, state.names) {
        val index = state.names.indexOfFirst { it.number == position }
        if (index >= 0 && index != pager.settledPage) pager.scrollToPage(index)
    }
    LaunchedEffect(pager) {
        // A tick as each name comes in, not on opening
        snapshotFlow { pager.currentPage }.drop(1).collect { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    }
    val shownNumber = state.names.getOrNull(pager.currentPage)?.number ?: position

    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SoftTopBar("", showTitle = false, onBack = onBack) {
                    if (state.names.isNotEmpty()) {
                        SoftIconButton(R.drawable.ic_export_arrow_01_linear, "Share", { onIntent(NamesIntent.ShareTapped) }, size = 44.dp, iconSize = 22.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    SoftIconButton(R.drawable.ic_search_normal_linear, "Search names", onOpenSearch, Modifier.padding(end = 12.dp), size = 44.dp, iconSize = 22.dp)
                }
            },
        ) { padding ->
            BoxWithConstraints(Modifier.padding(top = padding.calculateTopPadding())) {
                // 300dp on a large phone, in proportion on bigger and smaller screens, as on iOS
                val cardHeight = max(260f, maxHeight.value * 0.415f).dp
                Column(
                    // Clear of the system bar, which the page's backdrop runs under
                    Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).fillMaxWidth().navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("99 Names", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.headlineMedium, color = soft.appPrimary)
                        HorizontalPager(
                            state = pager,
                            modifier = Modifier.padding(top = 28.dp),
                            contentPadding = PaddingValues(horizontal = 38.dp),
                            pageSpacing = 12.dp,
                            beyondViewportPageCount = 1,
                            // The page count follows the latest state while this lambda may still hold an earlier one, e.g.
                            // the empty list before the names load, so a page it has no name for gets a key of its own
                            key = { page -> state.names.getOrNull(page)?.number ?: -(page + 1) },
                        ) { page ->
                            val name = state.names.getOrNull(page) ?: return@HorizontalPager
                            NameCard(
                                name,
                                // Room for the card's float shadow
                                Modifier.padding(vertical = 18.dp).graphicsLayer {
                                    val offset = pager.currentPage - page + pager.currentPageOffsetFraction
                                    val away = offset.absoluteValue.coerceIn(0f, 1f)
                                    scaleX = lerp(1f, 0.88f, away)
                                    scaleY = scaleX
                                    alpha = lerp(1f, 0.6f, away)
                                    // Shrinks toward the middle card, so the peeking edge stays in view
                                    transformOrigin = TransformOrigin(if (offset > 0) 1f else 0f, 0.5f)
                                },
                                minHeight = cardHeight,
                            )
                        }
                    }
                    Column(Modifier.padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        SoftPillSurface(Modifier.height(34.dp)) {
                            AnimatedContent(
                                targetState = shownNumber,
                                transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                                label = "position",
                            ) { number ->
                                Text(
                                    "$number of ${max(state.names.size, 99)}",
                                    Modifier.padding(horizontal = 14.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                    color = soft.appPrimary,
                                )
                            }
                        }
                        state.hadith?.let { hadith ->
                            Column(
                                Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                TranslationText(hadith.text, color = soft.textSecondary, textAlign = TextAlign.Center, lineSpacing = 0.sp)
                                Text(hadith.source, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
                            }
                        }
                    }
                }
            }
        }
    }
}
