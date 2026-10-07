package com.muttaqi.android.feature.names

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSearchField
import com.muttaqi.android.designsystem.oneui.OneUiSegmented
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode
import com.muttaqi.shared.feature.names.presentation.NamesEffect
import com.muttaqi.shared.feature.names.presentation.NamesIntent
import com.muttaqi.shared.feature.names.presentation.NamesState
import com.muttaqi.shared.feature.names.presentation.NamesViewModel

@Composable
fun NamesSearchRoute(viewModel: NamesViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NamesEffect.ShowName -> onBack()
                is NamesEffect.OpenShare -> Unit
            }
        }
    }
    DisposableEffect(viewModel) { onDispose { viewModel.dispatch(NamesIntent.ClearQuery) } }
    NamesSearchScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun NamesSearchScreen(state: NamesState, onIntent: (NamesIntent) -> Unit, onBack: () -> Unit) {
    OneUiSurface {
        OneUiScaffold(title = "Search", onBack = onBack, expandable = false) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
            ) {
                item(key = "field") {
                    val focus = remember { FocusRequester() }
                    OneUiSearchField(
                        query = state.query,
                        onQueryChange = { onIntent(NamesIntent.QueryChanged(it)) },
                        onClear = { onIntent(NamesIntent.ClearQuery) },
                        placeholder = "Type here",
                        modifier = Modifier.focusRequester(focus),
                        keyboardType = if (state.searchMode == NameSearchMode.ByNumber) KeyboardType.Number else KeyboardType.Text,
                    )
                    LaunchedEffect(Unit) { focus.requestFocus() }
                }
                item(key = "modes") {
                    ModeButtons(
                        state.searchMode,
                        Modifier.padding(horizontal = OneUiDefaults.ScreenMargin).padding(top = 12.dp, bottom = OneUiDefaults.GroupGap),
                    ) { onIntent(NamesIntent.SearchModeChanged(it)) }
                }
                if (state.isSearching && state.results.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "No results for “${state.query}”",
                            Modifier.fillMaxWidth().padding(24.dp),
                            style = OneUi.typography.listSummary,
                            color = OneUi.colors.secondaryText,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                itemsIndexed(state.results, key = { _, name -> name.number }) { index, name ->
                    NameResultRow(
                        name,
                        first = index == 0,
                        last = index == state.results.lastIndex,
                        onClick = { onIntent(NamesIntent.ResultTapped(name.number)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButtons(mode: NameSearchMode, modifier: Modifier = Modifier, onSelect: (NameSearchMode) -> Unit) {
    val modes = listOf(NameSearchMode.ByNumber to "by Number", NameSearchMode.ByName to "by Name (eng)")
    OneUiSegmented(
        options = modes.map { it.second },
        selected = modes.indexOfFirst { it.first == mode },
        onSelect = { index -> modes[index].first.let { option -> if (option != mode) onSelect(option) } },
        modifier = modifier,
    )
}

@Composable
private fun NameResultRow(name: AllahName, first: Boolean, last: Boolean, onClick: () -> Unit) {
    val colors = OneUi.colors
    val radius = OneUiDefaults.ContainerRadius
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = OneUiDefaults.ScreenMargin)
            .clip(RoundedCornerShape(topStart = if (first) radius else 0.dp, topEnd = if (first) radius else 0.dp, bottomStart = if (last) radius else 0.dp, bottomEnd = if (last) radius else 0.dp))
            .background(colors.container)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = OneUiDefaults.ItemPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NameNumber(name.number)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name.transliteration, style = OneUi.typography.listTitle, color = colors.text)
                TranslationText(name.meaning, Modifier.fillMaxWidth(), style = OneUi.typography.listSummary, color = colors.secondaryText)
            }
            Spacer(Modifier.width(12.dp))
            ArabicText(name.arabic, style = OneUi.typography.listTitle.copy(fontSize = 22.sp), color = colors.accent)
        }
        if (!last) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = OneUiDefaults.ItemPadding + 56.dp, end = OneUiDefaults.ItemPadding)
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
    }
}
