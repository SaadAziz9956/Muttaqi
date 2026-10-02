package com.muttaqi.android.feature.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftSearchField
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.presentation.list.JournalListEffect
import com.muttaqi.shared.feature.journal.presentation.list.JournalListIntent
import com.muttaqi.shared.feature.journal.presentation.list.JournalListState
import com.muttaqi.shared.feature.journal.presentation.list.JournalListViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

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
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }
    val searchActive = isSearchFocused || state.query.isNotEmpty()
    val entries = state.shownEntries

    Box(Modifier.fillMaxSize().background(journalBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AnimatedVisibility(!searchActive, enter = fadeIn(), exit = fadeOut()) {
                    JournalTopBar("Journal", showTitle = list.firstVisibleItemIndex > 0, onBack = onBack)
                }
            },
            bottomBar = {
                JournalSearchBar(
                    query = state.query,
                    searchActive = searchActive,
                    onIntent = onIntent,
                    onFocusChanged = { isSearchFocused = it },
                    onCloseSearch = {
                        onIntent(JournalListIntent.QueryChanged(""))
                        focusManager.clearFocus()
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                state = list,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
            ) {
                if (!state.isSearching) {
                    item(key = "header") { JournalHeader(state) }
                }
                itemsIndexed(entries, key = { _, entry -> entry.id }) { _, entry ->
                    JournalEntryRow(
                        entry,
                        onClick = { onIntent(JournalListIntent.EntryTapped(entry.id)) },
                        onDelete = { onIntent(JournalListIntent.DeleteTapped(entry.id)) },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "empty") {
                    if (state.isSearching && entries.isEmpty()) {
                        EmptyState(
                            icon = R.drawable.ic_search_normal_linear,
                            iconTint = MuttaqiTheme.soft.textSecondary,
                            title = "No Results for “${state.query}”",
                            description = "Check the spelling or try a new search.",
                        )
                    } else if (!state.isLoading && !state.hasEntries) {
                        EmptyState(
                            icon = R.drawable.ic_home_journal,
                            iconTint = MuttaqiTheme.soft.brandTeal,
                            title = "No entries yet",
                            description = "Write down what you're grateful for today. Tap + to start.",
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalHeader(state: JournalListState) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        PageHeader("Journal", state.header, Modifier.padding(top = 24.dp))
        if (state.hasEntries) {
            Text(
                "Notes",
                Modifier.padding(top = 48.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MuttaqiTheme.soft.textPrimary,
            )
        }
    }
}

private enum class Swipe { Closed, Open }

private val DeleteActionWidth = 76.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JournalEntryRow(entry: JournalEntry, onClick: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val scope = rememberCoroutineScope()
    val revealPx = with(LocalDensity.current) { DeleteActionWidth.toPx() }
    val swipe = remember(revealPx) {
        AnchoredDraggableState(Swipe.Closed, DraggableAnchors { Swipe.Closed at 0f; Swipe.Open at -revealPx })
    }
    val offset = swipe.offset.takeUnless { it.isNaN() } ?: 0f
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        if (offset < 0f) {
            DeleteAction(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterEnd).width(DeleteActionWidth).graphicsLayer { alpha = (-offset / revealPx).coerceIn(0f, 1f) },
            )
        }
        Column(
            Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .anchoredDraggable(swipe, Orientation.Horizontal)
                .then(
                    if (offset < 0f) {
                        Modifier.background(if (soft.dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA), RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
                    } else {
                        Modifier
                    },
                )
                .combinedClickable(
                    onClick = { if (swipe.currentValue == Swipe.Open) scope.launch { swipe.animateTo(Swipe.Closed) } else onClick() },
                    onLongClick = { showMenu = true },
                )
                .padding(horizontal = 24.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(entry.createdAt.dayAndMonth(), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp), color = soft.brandTeal, maxLines = 1)
                    Text(entry.createdAt.weekdayAndYear(), style = MaterialTheme.typography.labelSmall, color = soft.textSecondary, maxLines = 1)
                }
                Text(
                    entry.preview,
                    Modifier.weight(1f).padding(start = 15.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = soft.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(12.dp))
                Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.padding(end = 8.dp).size(24.dp), tint = soft.textPrimary)
            }
            HorizontalDivider(thickness = 0.5.dp, color = journalDivider)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Delete", color = destructiveRed) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_trash_linear), null, Modifier.size(20.dp), tint = destructiveRed) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun DeleteAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(50.dp).background(destructiveRed, CircleShape).combinedClickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_trash_linear), "Delete", Modifier.size(24.dp), tint = Color.White)
        }
        Text("Delete", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp), color = MuttaqiTheme.soft.textSecondary)
    }
}

@Composable
private fun JournalSearchBar(
    query: String,
    searchActive: Boolean,
    onIntent: (JournalListIntent) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onCloseSearch: () -> Unit,
) {
    val soft = MuttaqiTheme.soft
    val turn by animateFloatAsState(if (searchActive) 45f else 0f, label = "newEntryTurn")
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SoftSearchField(
            query = query,
            onQueryChange = { onIntent(JournalListIntent.QueryChanged(it)) },
            onClear = { onIntent(JournalListIntent.QueryChanged("")) },
            modifier = Modifier.weight(1f).onFocusChanged { onFocusChanged(it.hasFocus) },
        )
        JournalBarButton(
            R.drawable.ic_add_linear,
            if (searchActive) "Close search" else "New entry",
            onClick = { if (searchActive) onCloseSearch() else onIntent(JournalListIntent.NewEntryTapped) },
            modifier = Modifier.graphicsLayer { rotationZ = turn },
            tint = if (searchActive) soft.textPrimary else soft.brandTeal,
            size = 50.dp,
        )
    }
}

@Composable
private fun EmptyState(icon: Int, iconTint: Color, title: String, description: String, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(
        modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(icon), null, Modifier.size(44.dp), tint = iconTint)
        Text(
            title,
            Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
            color = soft.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Default, lineHeight = 24.sp),
            color = soft.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
