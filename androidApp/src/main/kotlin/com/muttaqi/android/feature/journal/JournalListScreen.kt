package com.muttaqi.android.feature.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiFab
import com.muttaqi.android.designsystem.oneui.OneUiMenu
import com.muttaqi.android.designsystem.oneui.OneUiMenuItem
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSearchField
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted
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

@Composable
fun JournalListScreen(
    state: JournalListState,
    onIntent: (JournalListIntent) -> Unit,
    onBack: () -> Unit,
) {
    val list = rememberLazyListState()
    val atTop by remember { derivedStateOf { list.firstVisibleItemIndex == 0 } }
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val entries = state.shownEntries

    OneUiSurface {
        OneUiScaffold(
            title = "Journal",
            onBack = onBack,
            subtitle = state.header?.let { header -> { OneUiHeaderQuote(header.text, header.source) } },
            floatingActionButton = {
                Box(
                    Modifier
                        .offset(x = FabShadowRoom, y = FabShadowRoom)
                        .animateContentSize(tween(250, easing = OneUiDefaults.Easing))
                        .padding(FabShadowRoom),
                ) {
                    OneUiFab(
                        icon = R.drawable.ic_add,
                        description = "New entry",
                        onClick = { onIntent(JournalListIntent.NewEntryTapped) },
                        text = if (atTop) "New entry" else null,
                    )
                }
            },
        ) { padding ->
            LazyColumn(
                state = list,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 96.dp),
            ) {
                item(key = "search") {
                    OneUiSearchField(
                        query = state.query,
                        onQueryChange = { onIntent(JournalListIntent.QueryChanged(it)) },
                        onClear = { onIntent(JournalListIntent.QueryChanged("")) },
                        placeholder = "Search",
                        modifier = Modifier.padding(bottom = OneUiDefaults.GroupGap),
                    )
                }
                if (!state.isSearching && state.hasEntries) {
                    item(key = "notes") { OneUiSubheader("Notes") }
                }
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    JournalEntryRow(
                        entry,
                        shape = groupItemShape(index, entries.size),
                        divider = index < entries.lastIndex,
                        onClick = { onIntent(JournalListIntent.EntryTapped(entry.id)) },
                        onDelete = { pendingDelete = entry.id },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "empty") {
                    if (state.isSearching && entries.isEmpty()) {
                        EmptyState(
                            icon = R.drawable.ic_search,
                            iconTint = OneUi.colors.secondaryText,
                            title = "No Results for “${state.query}”",
                            description = "Check the spelling or try a new search.",
                        )
                    } else if (!state.isLoading && !state.hasEntries) {
                        EmptyState(
                            icon = R.drawable.ic_edit_note,
                            iconTint = OneUi.colors.accent,
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
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    shape: Shape,
    divider: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val swipe = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ScreenMargin).clip(shape)) {
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
                        Modifier.fillMaxSize().background(colors.destructive).padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = colors.container)
                    }
                }
            },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.container)
                    .combinedClickable(onClick = onClick, onLongClick = { showMenu = true }),
            ) {
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = OneUiDefaults.ItemPadding, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(entry.preview, style = type.listTitle, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${entry.createdAt.dayAndMonth()} · ${entry.createdAt.weekdayAndYear()}",
                        style = type.listSummary,
                        color = colors.secondaryText,
                    )
                }
                if (divider) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ItemPadding).height(1.dp).background(colors.divider))
                }
            }
        }
        OneUiMenu(expanded = showMenu, onDismiss = { showMenu = false }) {
            OneUiMenuItem(
                text = "Delete",
                onClick = {
                    showMenu = false
                    onDelete()
                },
                icon = R.drawable.ic_delete,
                destructive = true,
            )
        }
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
        Text(title, Modifier.padding(top = 8.dp), style = OneUi.typography.dialogTitle, color = OneUi.colors.text, textAlign = TextAlign.Center)
        Text(description, style = OneUi.typography.listSummary, color = OneUi.colors.secondaryText, textAlign = TextAlign.Center)
    }
}

private val FabShadowRoom = 16.dp
