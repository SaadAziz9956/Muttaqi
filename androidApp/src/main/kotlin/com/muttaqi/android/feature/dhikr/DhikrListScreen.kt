package com.muttaqi.android.feature.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.PageHeader
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftChip
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListEffect
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListIntent
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListState
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DhikrListRoute(onOpenCounter: (String) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DhikrListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DhikrListEffect.OpenCounter -> onOpenCounter(effect.dhikrId)
            }
        }
    }
    DhikrListScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun DhikrListScreen(state: DhikrListState, onIntent: (DhikrListIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val list = rememberLazyListState()
    val section = state.selectedSection
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SoftTopBar(TITLE, showTitle = list.firstVisibleItemIndex > 0, onBack = onBack) },
        ) { padding ->
            LazyColumn(
                state = list,
                contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 32.dp),
            ) {
                item { PageHeader(TITLE, state.header, Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)) }
                if (section != null) {
                    item { SectionChips(state, selectedId = section.id, onIntent) }
                    item {
                        Text(
                            section.subtitle,
                            Modifier.padding(start = 24.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = soft.textSecondary,
                        )
                    }
                    items(section.dhikr, key = { it.id }) { dhikr ->
                        DhikrRow(
                            dhikr,
                            onClick = { onIntent(DhikrListIntent.DhikrTapped(dhikr.id)) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionChips(state: DhikrListState, selectedId: String, onIntent: (DhikrListIntent) -> Unit) {
    val row = rememberLazyListState()
    val selectedIndex = state.sections.indexOfFirst { it.id == selectedId }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val viewport = row.layoutInfo.viewportSize.width
        val chip = row.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }?.size ?: 0
        row.animateScrollToItem(selectedIndex, scrollOffset = -(viewport - chip) / 2)
    }
    LazyRow(
        state = row,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.sections, key = { it.id }) { section ->
            SoftChip(section.title, selected = section.id == selectedId, onClick = { onIntent(DhikrListIntent.SectionTapped(section.id)) })
        }
    }
}

@Composable
private fun DhikrRow(dhikr: Dhikr, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val caption = dhikr.title ?: dhikr.translation?.quoted() ?: dhikr.transliteration.orEmpty()
    SoftCard(
        modifier.clearAndSetSemantics {
            contentDescription = dhikr.title ?: dhikr.transliteration ?: dhikr.arabic
            dhikr.translation?.let { stateDescription = it }
        },
        cornerRadius = 24.dp,
        onClick = onClick,
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuranText(dhikr.arabic, Modifier.fillMaxWidth(), fontSize = 21.sp, textAlign = TextAlign.Right, lineSpacing = 0.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TranslationText(
                    caption,
                    Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = soft.appPrimary,
                    textAlign = if (caption.isArabicScript()) TextAlign.Right else TextAlign.Left,
                    lineSpacing = 0.sp,
                )
                dhikr.target?.let { target -> CountPill("$target×") }
            }
        }
    }
}

@Composable
internal fun CountPill(text: String) {
    val soft = MuttaqiTheme.soft
    Box(Modifier.height(24.dp).background(soft.tintedSurface, CircleShape).padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = soft.brandTeal)
    }
}

private const val TITLE = "Zikr o Azkar"
