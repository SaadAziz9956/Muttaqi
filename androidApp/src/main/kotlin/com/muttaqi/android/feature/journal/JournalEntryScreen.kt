package com.muttaqi.android.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiButtonStyle
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.android.designsystem.oneui.OneUiTextField
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
    val focusManager = LocalFocusManager.current
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    var titleFocused by remember { mutableStateOf(false) }
    var bodyFocused by remember { mutableStateOf(false) }
    val editing = titleFocused || bodyFocused
    var wasEditing by remember { mutableStateOf(false) }
    val entry = state.entry
    val colors = OneUi.colors
    val type = OneUi.typography
    val titleStyle = type.listTitle.copy(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold)

    LaunchedEffect(editing) {
        if (wasEditing && !editing) onIntent(JournalEntryIntent.SaveNow)
        wasEditing = editing
    }
    LaunchedEffect(state.startedEmpty) {
        if (state.startedEmpty) titleFocus.requestFocus()
    }

    OneUiSurface {
        OneUiScaffold(
            title = entry?.createdAt?.longDate().orEmpty(),
            onBack = onBack,
            subtitle = entry?.let { shown -> { Text(shown.createdAt.weekday(), style = type.listSummary, color = colors.secondaryText) } },
            expandable = !editing,
            actions = {
                if (editing) {
                    OneUiButton("Done", onClick = { focusManager.clearFocus() }, style = OneUiButtonStyle.Text)
                } else {
                    OneUiIconButton(R.drawable.ic_delete, "Delete entry", onClick = { onIntent(JournalEntryIntent.DeleteTapped) })
                }
            },
        ) { padding ->
            if (entry != null) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
                ) {
                    OneUiGroup {
                        OneUiTextField(
                            value = state.title,
                            onValueChange = { title ->
                                onIntent(JournalEntryIntent.TitleChanged(title))
                                if (title.contains('\n')) bodyFocus.requestFocus()
                            },
                            placeholder = "Title",
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(titleFocus)
                                .onFocusChanged { titleFocused = it.isFocused },
                            textStyle = titleStyle,
                            singleLine = false,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { bodyFocus.requestFocus() }),
                            contentPadding = PaddingValues(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 20.dp, bottom = 14.dp),
                            filled = false,
                        )
                        Box(Modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ItemPadding).height(1.dp).background(colors.divider))
                        OneUiTextField(
                            value = state.body,
                            onValueChange = { onIntent(JournalEntryIntent.BodyChanged(it)) },
                            placeholder = "Body",
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(bodyFocus)
                                .onFocusChanged { bodyFocused = it.isFocused },
                            textStyle = type.body,
                            singleLine = false,
                            minLines = 8,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            contentPadding = PaddingValues(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 14.dp, bottom = 20.dp),
                            filled = false,
                        )
                    }

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
}
