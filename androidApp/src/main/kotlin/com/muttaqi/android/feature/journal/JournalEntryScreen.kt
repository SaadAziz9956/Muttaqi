package com.muttaqi.android.feature.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryEffect
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryIntent
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryState
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun JournalEntryRoute(entryId: String?, onBack: () -> Unit) {
    val viewModel = koinViewModel<JournalEntryViewModel> { parametersOf(entryId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                JournalEntryEffect.Close -> onBack()
            }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.dispatch(JournalEntryIntent.SaveNow) }
    DisposableEffect(viewModel) {
        onDispose { viewModel.dispatch(JournalEntryIntent.SaveNow) }
    }
    JournalEntryScreen(state, viewModel::dispatch, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(state: JournalEntryState, onIntent: (JournalEntryIntent) -> Unit, onBack: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    var titleFocused by remember { mutableStateOf(false) }
    var bodyFocused by remember { mutableStateOf(false) }
    val editing = titleFocused || bodyFocused
    var wasEditing by remember { mutableStateOf(false) }
    val entry = state.entry
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(editing) {
        if (wasEditing && !editing) onIntent(JournalEntryIntent.SaveNow)
        wasEditing = editing
    }
    LaunchedEffect(state.startedEmpty) {
        if (state.startedEmpty) titleFocus.requestFocus()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (editing) {
                        TextButton(onClick = { focusManager.clearFocus() }) { Text("Done") }
                    } else {
                        IconButton(onClick = { onIntent(JournalEntryIntent.DeleteTapped) }) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = "Delete entry")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        if (entry != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    entry.createdAt.entryDate(),
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TextField(
                    value = state.title,
                    onValueChange = { title ->
                        onIntent(JournalEntryIntent.TitleChanged(title))
                        if (title.contains('\n')) bodyFocus.requestFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .focusRequester(titleFocus)
                        .onFocusChanged { titleFocused = it.isFocused },
                    textStyle = MaterialTheme.typography.headlineSmall,
                    placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { bodyFocus.requestFocus() }),
                )

                TextField(
                    value = state.body,
                    onValueChange = { onIntent(JournalEntryIntent.BodyChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .focusRequester(bodyFocus)
                        .onFocusChanged { bodyFocused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("Body") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    minLines = 8,
                )

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { bodyFocus.requestFocus() },
                )
            }
        }
    }

    if (state.isConfirmingDelete) {
        DeleteEntryDialog(
            onConfirm = { onIntent(JournalEntryIntent.DeleteConfirmed) },
            onDismiss = { onIntent(JournalEntryIntent.DeleteCancelled) },
        )
    }
}
