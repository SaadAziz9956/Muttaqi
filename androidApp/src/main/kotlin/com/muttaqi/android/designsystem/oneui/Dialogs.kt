package com.muttaqi.android.designsystem.oneui

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun OneUiDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissText: String = "Cancel",
    destructive: Boolean = false,
) {
    OneUiBottomDialog(onDismiss, modifier) {
        Text(title, style = OneUi.typography.dialogTitle, color = OneUi.colors.text)
        if (text != null) Text(text, style = OneUi.typography.dialogText, color = OneUi.colors.secondaryText)
        OneUiDialogButtons(
            dismissText = dismissText,
            onDismiss = onDismiss,
            confirmText = confirmText,
            onConfirm = onConfirm,
            destructive = destructive,
        )
    }
}

@Composable
fun OneUiBottomDialog(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.setGravity(Gravity.BOTTOM)
            window?.setDimAmount(0.4f)
        }
        Column(
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(OneUiDefaults.ContainerRadius))
                .background(OneUi.colors.container)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun OneUiDialogButtons(dismissText: String, onDismiss: () -> Unit, confirmText: String, onConfirm: () -> Unit, destructive: Boolean = false) {
    val colors = OneUi.colors
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        OneUiButton(dismissText, onDismiss, Modifier.weight(1f), style = OneUiButtonStyle.Text, color = colors.text)
        Box(Modifier.width(1.dp).height(16.dp).background(colors.divider))
        OneUiButton(confirmText, onConfirm, Modifier.weight(1f), style = OneUiButtonStyle.Text, color = if (destructive) colors.destructive else colors.accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val page = OneUi.colors
    val colors = if (page.isDark) page.copy(background = page.container, container = SheetDarkContainer) else page
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background,
        contentColor = colors.text,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).size(width = 40.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(colors.secondaryText.copy(alpha = 0.5f)))
        },
    ) {
        CompositionLocalProvider(LocalOneUiColors provides colors) { content() }
    }
}

@Composable
fun OneUiMenu(expanded: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier, offset: DpOffset = DpOffset(0.dp, 0.dp), content: @Composable ColumnScope.() -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier,
        offset = offset,
        shape = RoundedCornerShape(20.dp),
        containerColor = OneUi.colors.container,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        content = content,
    )
}

@Composable
fun OneUiMenuItem(text: String, onClick: () -> Unit, icon: Int? = null, destructive: Boolean = false) {
    val color = if (destructive) OneUi.colors.destructive else OneUi.colors.text
    DropdownMenuItem(
        text = { Text(text, style = OneUi.typography.listTitle.copy(fontSize = OneUi.typography.field.fontSize), color = color) },
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(painterResource(it), contentDescription = null, Modifier.size(22.dp), tint = color) } },
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
    )
}

@Composable
fun OneUiSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data -> OneUiSnackbar(data) }
}

@Composable
fun OneUiSnackbar(data: SnackbarData) {
    val colors = OneUi.colors
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(colors.toast)
            .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(data.visuals.message, Modifier.weight(1f, fill = false), style = OneUi.typography.listSummary, color = colors.onToast, maxLines = 3)
        data.visuals.actionLabel?.let { label ->
            OneUiButton(label, { data.performAction() }, style = OneUiButtonStyle.Text, color = colors.accent)
        }
    }
}

private val SheetDarkContainer = Color(0xFF26262A)
