package com.muttaqi.android.feature.topics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.muttaqi.android.designsystem.component.softFloat
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

/** Shown in the bar */
internal val TopicChips.title: String
    get() = when (this) {
        TopicChips.Emotions -> "Emotions"
        TopicChips.ExploreGroup -> "Explore"
    }

/**
 * One topic's verses, hadith or duas, with chips to move to its neighbours and a segmented control for the kinds of
 * text it has
 */
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
                    // Moving to another topic fades, as on iOS; switching the kind of text doesn't
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

/** The topics as chips; the one shown is scrolled to the middle of the row */
@Composable
private fun TopicChipRow(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit) {
    val row = rememberLazyListState()
    val selectedIndex = state.topics.indexOfFirst { it.id == state.selectedId }
    // Straight there once the topics load, then with a spring as other chips are tapped
    val loaded = state.topics.isNotEmpty()
    LaunchedEffect(loaded) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = false) }
    LaunchedEffect(state.selectedId) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = true) }

    LazyRow(
        state = row,
        // Room for the chips' float shadow
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

/**
 * The kinds of text as a segmented control, as iOS draws one: a tinted capsule with the selected segment on a raised
 * thumb that springs across
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopicSectionPicker(
    sections: List<TopicSection>,
    selected: TopicSection,
    onSelect: (TopicSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soft = MuttaqiTheme.soft
    val track = if (soft.dark) Color(0x3D767680) else Color(0x1F767680)
    val thumb = if (soft.dark) Color(0xFF5A5A5E) else Color.White
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(CircleShape)
            .background(track)
            .padding(2.dp),
    ) {
        val segment = maxWidth / sections.size
        val offset by animateDpAsState(
            segment * sections.indexOf(selected).coerceAtLeast(0),
            MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "thumb",
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .softFloat(CircleShape, elevation = 2.dp)
                .background(thumb, CircleShape),
        )
        Row(Modifier.fillMaxSize().selectableGroup()) {
            sections.forEach { section ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(selected = section == selected, role = Role.Tab, onClick = { onSelect(section) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        section.title,
                        style = TextStyle(fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = soft.textPrimary,
                    )
                }
            }
        }
    }
}

/** The shown topic's texts of the selected kind, each on a card, and the translations credited */
@Composable
private fun TopicPassages(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state.passages.forEach { passage ->
            PassageCard(passage, onShare = { onIntent(TopicPageIntent.ShareTapped(passage.id)) })
        }
        // Names whose translations are shown, as their publishers ask
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

/** A verse, hadith or dua: its Arabic, translation and where it's from, centred, with a share button */
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
                // An Urdu attribution, e.g. "اسے امام بخاری نے روایت کیا ہے", is set in Nastaliq like its translation
                if (passage.source.isArabicScript()) {
                    TranslationText(passage.source, Modifier.weight(1f, fill = false), fontSize = 11.sp, color = soft.brandTeal)
                } else {
                    Text(passage.source, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.labelSmall, color = soft.brandTeal, textAlign = TextAlign.Center)
                }
                ShareIcon(onShare)
            }
        }
    }
}

/** A plain share icon with the soft press, as on the topic page's cards on iOS */
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
