package com.muttaqi.android.feature.names

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.PageSearchBar
import com.muttaqi.android.designsystem.component.TranslationText
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesSearchScreen(state: NamesState, onIntent: (NamesIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        ) {
            item(key = "field") {
                PageSearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(NamesIntent.QueryChanged(it)) },
                    onClear = { onIntent(NamesIntent.ClearQuery) },
                    placeholder = "Type here",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    keyboardType = if (state.searchMode == NameSearchMode.ByNumber) KeyboardType.Number else KeyboardType.Text,
                    autoFocus = true,
                )
            }
            item(key = "modes") {
                ModeButtons(state.searchMode, Modifier.padding(16.dp)) { onIntent(NamesIntent.SearchModeChanged(it)) }
            }
            if (state.isSearching && state.results.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "No results for “${state.query}”",
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(state.results, key = { it.number }) { name ->
                ListItem(
                    headlineContent = { Text(name.transliteration) },
                    supportingContent = {
                        TranslationText(
                            name.meaning,
                            Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = { NameNumber(name.number) },
                    trailingContent = {
                        ArabicText(name.arabic, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clickable { onIntent(NamesIntent.ResultTapped(name.number)) },
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeButtons(mode: NameSearchMode, modifier: Modifier = Modifier, onSelect: (NameSearchMode) -> Unit) {
    val modes = listOf(NameSearchMode.ByNumber to "by Number", NameSearchMode.ByName to "by Name (eng)")
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        modes.forEachIndexed { index, (option, label) ->
            ToggleButton(
                checked = option == mode,
                onCheckedChange = { if (option != mode) onSelect(option) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes() else ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                Text(label)
            }
        }
    }
}
