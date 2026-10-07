package com.muttaqi.android.designsystem.oneui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.quoted

@Composable
fun OneUiGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = OneUiDefaults.ScreenMargin)
            .clip(RoundedCornerShape(OneUiDefaults.ContainerRadius))
            .background(OneUi.colors.container),
        content = content,
    )
}

@Composable
fun OneUiSubheader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .fillMaxWidth()
            .padding(start = OneUiDefaults.ScreenMargin + OneUiDefaults.ItemPadding, end = OneUiDefaults.ScreenMargin + OneUiDefaults.ItemPadding, bottom = 8.dp)
            .semantics { heading() },
        style = OneUi.typography.subheader,
        color = OneUi.colors.secondaryText,
    )
}

@Composable
fun OneUiListRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    divider: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = OneUi.colors
    Column(modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = OneUiDefaults.ItemPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(LEADING_GAP))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = OneUi.typography.listTitle, color = colors.text)
                summary?.let { Text(it, style = OneUi.typography.listSummary, color = colors.secondaryText) }
            }
            trailing?.let {
                Spacer(Modifier.width(12.dp))
                it()
            }
        }
        if (divider) OneUiDivider(start = if (leading != null) OneUiDefaults.ItemPadding + OneUiDefaults.BadgeSize + LEADING_GAP else OneUiDefaults.ItemPadding)
    }
}

@Composable
fun OneUiDivider(start: Dp = OneUiDefaults.ItemPadding, end: Dp = OneUiDefaults.ItemPadding) {
    Box(Modifier.fillMaxWidth().padding(start = start, end = end).height(1.dp).background(OneUi.colors.divider))
}

fun Modifier.oneUiGroupItem(index: Int, count: Int, container: Color): Modifier {
    val radius = OneUiDefaults.ContainerRadius
    val shape = RoundedCornerShape(
        topStart = if (index == 0) radius else 0.dp,
        topEnd = if (index == 0) radius else 0.dp,
        bottomStart = if (index == count - 1) radius else 0.dp,
        bottomEnd = if (index == count - 1) radius else 0.dp,
    )
    return padding(horizontal = OneUiDefaults.ScreenMargin).clip(shape).background(container)
}

@Composable
fun OneUiBadge(modifier: Modifier = Modifier, size: Dp = OneUiDefaults.BadgeSize, content: @Composable () -> Unit) {
    val colors = OneUi.colors
    CompositionLocalProvider(LocalContentColor provides colors.accent) {
        Box(
            modifier.size(size).clip(CircleShape).background(colors.accent.copy(alpha = if (colors.isDark) 0.18f else 0.1f)),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

@Composable
fun OneUiHeaderQuote(text: String, source: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TranslationText(
            text.quoted(),
            Modifier.fillMaxWidth(),
            style = OneUi.typography.listSummary,
            color = OneUi.colors.secondaryText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(source, style = OneUi.typography.small, color = OneUi.colors.accent, textAlign = TextAlign.Center)
    }
}

private val LEADING_GAP = 16.dp

@Composable
fun OneUiSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    autoFocus: Boolean = false,
) {
    val colors = OneUi.colors
    var field by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
    LaunchedEffect(query) { if (field.text != query) field = TextFieldValue(query, TextRange(query.length)) }
    val focus = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
    BasicTextField(
        value = field,
        onValueChange = { value ->
            field = value
            if (value.text != query) onQueryChange(value.text)
        },
        modifier = modifier.fillMaxWidth().padding(horizontal = OneUiDefaults.ScreenMargin).focusRequester(focus),
        singleLine = true,
        textStyle = OneUi.typography.field.copy(color = colors.text),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Search),
        decorationBox = { field ->
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(colors.container).padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null, Modifier.size(20.dp), tint = colors.secondaryText)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text(placeholder, style = OneUi.typography.field, color = colors.secondaryText, maxLines = 1)
                    field()
                }
                if (query.isNotEmpty()) {
                    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClear), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_close), contentDescription = "Clear search", Modifier.size(20.dp), tint = colors.secondaryText)
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
            }
        },
    )
}

@Composable
fun OneUiCountPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.clip(CircleShape).background(OneUi.colors.component).padding(horizontal = 10.dp, vertical = 3.dp),
        style = OneUi.typography.small,
        color = OneUi.colors.secondaryText,
    )
}
