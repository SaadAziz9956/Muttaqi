package com.muttaqi.android.feature.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUiChip
import com.muttaqi.android.designsystem.oneui.OneUiCountPill
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
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
    val section = state.selectedSection
    OneUiSurface {
        OneUiScaffold(
            title = TITLE,
            onBack = onBack,
            subtitle = state.header?.let { header -> { OneUiHeaderQuote(header.text, header.source) } },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(OneUiDefaults.GroupGap),
            ) {
                if (section != null) {
                    item(key = "sections") { SectionChips(state, selectedId = section.id, onIntent) }
                    item(key = "section-${section.id}") { SectionGroup(section, onIntent) }
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
        modifier = Modifier.selectableGroup(),
        contentPadding = PaddingValues(horizontal = OneUiDefaults.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.sections, key = { it.id }) { section ->
            OneUiChip(
                text = section.title,
                selected = section.id == selectedId,
                onClick = { onIntent(DhikrListIntent.SectionTapped(section.id)) },
            )
        }
    }
}

@Composable
private fun SectionGroup(section: DhikrSection, onIntent: (DhikrListIntent) -> Unit) {
    Column {
        OneUiSubheader(section.subtitle)
        OneUiGroup {
            section.dhikr.forEachIndexed { index, dhikr ->
                DhikrRow(
                    dhikr,
                    divider = index < section.dhikr.lastIndex,
                    onClick = { onIntent(DhikrListIntent.DhikrTapped(dhikr.id)) },
                )
            }
        }
    }
}

@Composable
private fun DhikrRow(dhikr: Dhikr, divider: Boolean, onClick: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val transliteration = dhikr.transliteration?.takeIf { it.isNotBlank() }
    val caption = dhikr.title ?: dhikr.translation?.quoted()
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = dhikr.title ?: dhikr.transliteration ?: dhikr.arabic
                dhikr.translation?.let { stateDescription = it }
            },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ItemPadding, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArabicText(dhikr.arabic, Modifier.fillMaxWidth(), color = colors.text)
            transliteration?.let {
                TranslationText(it, Modifier.fillMaxWidth(), style = type.listSummary, color = colors.accent)
            }
            if (caption != null || dhikr.target != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (caption != null) {
                        TranslationText(caption, Modifier.weight(1f), style = type.listSummary, color = colors.secondaryText)
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    dhikr.target?.let { target -> OneUiCountPill("$target×") }
                }
            }
        }
        if (divider) {
            Box(Modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ItemPadding).height(1.dp).background(colors.divider))
        }
    }
}

private const val TITLE = "Zikr o Azkar"
