package com.muttaqi.android.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.designsystem.component.PageSearchBar
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.presentation.list.JournalListEffect
import com.muttaqi.shared.feature.journal.presentation.list.JournalListIntent
import com.muttaqi.shared.feature.journal.presentation.list.JournalListState
import com.muttaqi.shared.feature.journal.presentation.list.JournalListViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JournalListRoute(onOpenEntry: (String?) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<JournalListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is JournalListEffect.OpenEntry -> onOpenEntry(effect.entryId)
            }
        }
    }
    JournalListScreen(state, viewModel::dispatch, onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JournalListScreen(
    state: JournalListState,
    onIntent: (JournalListIntent) -> Unit,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val list = rememberLazyListState()
    val atTop by remember { derivedStateOf { list.firstVisibleItemIndex == 0 } }
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val entries = state.shownEntries

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Journal") },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New entry") },
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                onClick = { onIntent(JournalListIntent.NewEntryTapped) },
                expanded = atTop,
            )
        },
    ) { padding ->
        LazyColumn(
            state = list,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 96.dp),
        ) {
            state.header?.let { header -> item(key = "header") { PageQuote(header, Modifier.padding(horizontal = 16.dp)) } }
            item(key = "search") {
                PageSearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(JournalListIntent.QueryChanged(it)) },
                    onClear = { onIntent(JournalListIntent.QueryChanged("")) },
                    placeholder = "Search",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }
            if (!state.isSearching && state.hasEntries) {
                item(key = "notes") {
                    Text(
                        "Notes",
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            items(entries, key = { it.id }) { entry ->
                JournalEntryRow(
                    entry,
                    onClick = { onIntent(JournalListIntent.EntryTapped(entry.id)) },
                    onDelete = { pendingDelete = entry.id },
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "empty") {
                if (state.isSearching && entries.isEmpty()) {
                    EmptyState(
                        icon = R.drawable.ic_search,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "No Results for “${state.query}”",
                        description = "Check the spelling or try a new search.",
                    )
                } else if (!state.isLoading && !state.hasEntries) {
                    EmptyState(
                        icon = R.drawable.ic_edit_note,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "No entries yet",
                        description = "Write down what you're grateful for today. Tap + to start.",
                    )
                }
            }
        }
    }

    pendingDelete?.let { entryId ->
        DeleteEntryDialog(
            onConfirm = {
                pendingDelete = null
                onIntent(JournalListIntent.DeleteTapped(entryId))
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JournalEntryRow(entry: JournalEntry, onClick: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val swipe = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth()) {
        Box {
            SwipeToDismissBox(
                state = swipe,
                enableDismissFromStartToEnd = false,
                onDismiss = { direction ->
                    scope.launch { swipe.reset() }
                    if (direction == SwipeToDismissBoxValue.EndToStart) onDelete()
                },
                backgroundContent = {
                    if (swipe.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        Box(
                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                },
            ) {
                ListItem(
                    onClick = onClick,
                    onLongClick = { showMenu = true },
                    leadingContent = {
                        Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(entry.createdAt.dayAndMonth(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text(
                                entry.createdAt.weekdayAndYear(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    trailingContent = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
                ) {
                    Text(entry.preview, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error,
                    ),
                )
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun EmptyState(icon: Int, iconTint: Color, title: String, description: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, Modifier.size(48.dp), tint = iconTint)
        Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
