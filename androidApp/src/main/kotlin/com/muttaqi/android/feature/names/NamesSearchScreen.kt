package com.muttaqi.android.feature.names

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.SoftTopBar
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NamesSearchScreen(state: NamesState, onIntent: (NamesIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Box {
        SoftBackdrop()
        Scaffold(containerColor = Color.Transparent, topBar = { SoftTopBar("", showTitle = false, onBack = onBack) }) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Text("Search", style = MaterialTheme.typography.headlineMedium, color = soft.appPrimary) }
                item {
                    SoftPillSurface(Modifier.padding(top = 20.dp).fillMaxWidth().height(50.dp)) {
                        BasicTextField(
                            value = state.query,
                            onValueChange = { onIntent(NamesIntent.QueryChanged(it)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).focusRequester(focus),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = soft.textPrimary, textAlign = TextAlign.Center),
                            cursorBrush = SolidColor(soft.appPrimary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (state.searchMode == NameSearchMode.ByNumber) KeyboardType.Number else KeyboardType.Text,
                                imeAction = ImeAction.Search,
                            ),
                            decorationBox = { field ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (state.query.isEmpty()) {
                                        Text("Type here", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), color = soft.textSecondary)
                                    }
                                    field()
                                }
                            },
                        )
                    }
                }
                item { ModeButtons(state.searchMode) { onIntent(NamesIntent.SearchModeChanged(it)) } }
                if (state.isSearching && state.results.isEmpty()) {
                    item {
                        Text(
                            "No results for “${state.query}”",
                            Modifier.padding(top = 32.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = soft.textSecondary,
                        )
                    }
                }
                items(state.results, key = { it.number }) { name ->
                    NameCard(name, onClick = { onIntent(NamesIntent.ResultTapped(name.number)) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeButtons(mode: NameSearchMode, onSelect: (NameSearchMode) -> Unit) {
    val soft = MuttaqiTheme.soft
    val modes = listOf(NameSearchMode.ByNumber to "by Number", NameSearchMode.ByName to "by Name (eng)")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        modes.forEachIndexed { index, (option, label) ->
            ToggleButton(
                checked = option == mode,
                onCheckedChange = { if (option != mode) onSelect(option) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes() else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = soft.surface,
                    contentColor = soft.appPrimary,
                    checkedContainerColor = soft.brandGreen,
                    checkedContentColor = Color.White,
                ),
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
