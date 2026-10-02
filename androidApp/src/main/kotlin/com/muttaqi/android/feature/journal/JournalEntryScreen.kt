package com.muttaqi.android.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftPillSurface
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

@Composable
fun JournalEntryScreen(state: JournalEntryState, onIntent: (JournalEntryIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val focusManager = LocalFocusManager.current
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    var titleFocused by remember { mutableStateOf(false) }
    var bodyFocused by remember { mutableStateOf(false) }
    val editing = titleFocused || bodyFocused
    var wasEditing by remember { mutableStateOf(false) }
    val entry = state.entry

    LaunchedEffect(editing) {
        if (wasEditing && !editing) onIntent(JournalEntryIntent.SaveNow)
        wasEditing = editing
    }
    LaunchedEffect(state.startedEmpty) {
        if (state.startedEmpty) titleFocus.requestFocus()
    }

    Box(Modifier.fillMaxSize().background(journalBackground)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                JournalTopBar("", showTitle = false, onBack = onBack) {
                    if (editing) {
                        SoftPillSurface(Modifier.height(44.dp), onClick = { focusManager.clearFocus() }) {
                            Text("Done", Modifier.padding(horizontal = 18.dp), style = MaterialTheme.typography.titleSmall, color = soft.appPrimary)
                        }
                    } else {
                        JournalBarButton(R.drawable.ic_trash_linear, "Delete entry", { onIntent(JournalEntryIntent.DeleteTapped) }, iconSize = 22.dp)
                    }
                }
            },
        ) { padding ->
            if (entry != null) {
                val titleStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, color = soft.appPrimary)
                val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = (14 * 1.5f + 4).sp,
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both),
                    color = soft.textPrimary,
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(top = padding.calculateTopPadding())
                        .padding(horizontal = 20.dp),
                ) {
                    Text(entry.createdAt.entryDate(), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)

                    BasicTextField(
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
                        textStyle = titleStyle,
                        cursorBrush = SolidColor(soft.appPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { bodyFocus.requestFocus() }),
                        decorationBox = { field ->
                            if (state.title.isEmpty()) Text("Title", style = titleStyle, color = soft.appPrimary.copy(alpha = 0.35f))
                            field()
                        },
                    )

                    BasicTextField(
                        value = state.body,
                        onValueChange = { onIntent(JournalEntryIntent.BodyChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .focusRequester(bodyFocus)
                            .onFocusChanged { bodyFocused = it.isFocused },
                        textStyle = bodyStyle,
                        cursorBrush = SolidColor(soft.appPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        decorationBox = { field ->
                            if (state.body.isEmpty()) Text("Body", style = bodyStyle, color = soft.textSecondary)
                            field()
                        },
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
    }

    if (state.isConfirmingDelete) {
        AlertDialog(
            onDismissRequest = { onIntent(JournalEntryIntent.DeleteCancelled) },
            title = { Text("Delete this entry?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { onIntent(JournalEntryIntent.DeleteConfirmed) }) { Text("Delete Entry", color = destructiveRed) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(JournalEntryIntent.DeleteCancelled) }) { Text("Cancel", color = soft.appPrimary) }
            },
            containerColor = if (soft.dark) Color(0xFF1C1C1E) else Color.White,
        )
    }
}
