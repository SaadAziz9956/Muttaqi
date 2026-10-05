package com.muttaqi.android.feature.topics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftChip
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.component.softClickable
import com.muttaqi.android.designsystem.component.softPressScale
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageEffect
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageIntent
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageState
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicPassage
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TopicPageRoute(chips: TopicChips, topicId: String, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<TopicPageViewModel> { parametersOf(chips, topicId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TopicPageEffect.OpenShare -> onShare(effect.passage)
            }
        }
    }
    TopicPageScreen(chips.title, state, viewModel::dispatch, onBack)
}

internal val TopicChips.title: String
    get() = when (this) {
        TopicChips.Emotions -> "Emotions"
        TopicChips.ExploreGroup -> "Explore"
    }

@Composable
fun TopicPageScreen(title: String, state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, onBack: () -> Unit) {
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SoftTopBar(title, showTitle = true, onBack = onBack) },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding(), bottom = 32.dp),
            ) {
                TopicChipRow(state, onIntent)

                if (state.sections.size > 1) {
                    TopicSectionPicker(
                        sections = state.sections,
                        selected = state.section,
                        onSelect = { onIntent(TopicPageIntent.SectionTapped(it)) },
                        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 6.dp),
                    )
                }

                if (state.topics.isNotEmpty()) {
                    AnimatedContent(
                        targetState = state,
                        contentKey = { it.selectedId to it.section },
                        transitionSpec = {
                            if (initialState.selectedId != targetState.selectedId) {
                                fadeIn() togetherWith fadeOut()
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
                            }
                        },
                        label = "topic",
                    ) { shown ->
                        TopicPassages(shown, onIntent, Modifier.padding(horizontal = 20.dp).padding(top = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicChipRow(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit) {
    val row = rememberLazyListState()
    val selectedIndex = state.topics.indexOfFirst { it.id == state.selectedId }
    val loaded = state.topics.isNotEmpty()
    LaunchedEffect(loaded) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = false) }
    LaunchedEffect(state.selectedId) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = true) }

    LazyRow(
        state = row,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.topics, key = { it.id }) { topic ->
            SoftChip(
                topic.title,
                selected = topic.id == state.selectedId,
                onClick = { onIntent(TopicPageIntent.TopicTapped(topic.id)) },
            )
        }
    }
}

private suspend fun LazyListState.scrollToCenter(index: Int, animated: Boolean) {
    if (layoutInfo.visibleItemsInfo.none { it.index == index }) scrollToItem(index)
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    val distance = (item.offset + item.size / 2 - viewportCenter).toFloat()
    if (animated) animateScrollBy(distance) else scrollBy(distance)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopicSectionPicker(
    sections: List<TopicSection>,
    selected: TopicSection,
    onSelect: (TopicSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soft = MuttaqiTheme.soft
    val colors = ToggleButtonDefaults.toggleButtonColors(
        containerColor = if (soft.dark) Color(0x3D767680) else Color(0x1F767680),
        contentColor = soft.textPrimary,
        checkedContainerColor = if (soft.dark) Color(0xFF5A5A5E) else Color.White,
        checkedContentColor = soft.textPrimary,
    )
    Row(
        modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        sections.forEachIndexed { index, section ->
            ToggleButton(
                checked = section == selected,
                onCheckedChange = { onSelect(section) },
                modifier = Modifier
                    .weight(1f)
                    .height(ButtonDefaults.ExtraSmallContainerHeight)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    sections.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = colors,
                contentPadding = ButtonDefaults.ExtraSmallContentPadding,
            ) {
                Text(section.title, style = TextStyle(fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.Medium))
            }
        }
    }
}

@Composable
private fun TopicPassages(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state.passages.forEach { passage ->
            PassageCard(passage, onShare = { onIntent(TopicPageIntent.ShareTapped(passage.id)) })
        }
        if (state.translationCredits.isNotEmpty()) {
            Text(
                "Translation: " + state.translationCredits.joinToString(", "),
                Modifier.fillMaxWidth(),
                style = TextStyle(fontSize = 11.sp),
                color = soft.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PassageCard(passage: TopicPassage, onShare: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftCard(cornerRadius = 26.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (passage.arabic.isNotEmpty()) {
                ArabicText(passage.arabic, Modifier.fillMaxWidth())
            }
            TranslationText(passage.translation, Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (passage.source.isArabicScript()) {
                    TranslationText(passage.source, Modifier.weight(1f, fill = false), fontSize = 11.sp, color = soft.brandTeal, lineSpacing = 6.sp)
                } else {
                    Text(passage.source, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.labelSmall, color = soft.brandTeal, textAlign = TextAlign.Center)
                }
                ShareIcon(onShare)
            }
            passage.grade?.let { TranslationText(it, Modifier.fillMaxWidth(), fontSize = 11.sp, color = soft.textSecondary, lineSpacing = 4.sp) }
        }
    }
}

@Composable
private fun ShareIcon(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .softPressScale(interaction)
            .clip(CircleShape)
            .softClickable(interaction, onClick)
            .padding(6.dp),
    ) {
        Icon(painterResource(R.drawable.ic_export_arrow_01_linear), "Share", Modifier.size(16.dp), tint = MuttaqiTheme.soft.textSecondary)
    }
}
