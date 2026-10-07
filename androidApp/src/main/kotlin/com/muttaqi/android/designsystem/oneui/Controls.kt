package com.muttaqi.android.designsystem.oneui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OneUiButtonStyle { Filled, Tonal, Text }

@Composable
fun OneUiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: OneUiButtonStyle = OneUiButtonStyle.Filled,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    color: Color? = null,
) {
    val colors = OneUi.colors
    val (background, content) = when (style) {
        OneUiButtonStyle.Filled -> (color ?: colors.accent) to colors.onAccent
        OneUiButtonStyle.Tonal -> colors.component to (color ?: colors.text)
        OneUiButtonStyle.Text -> Color.Transparent to (color ?: colors.accent)
    }
    Row(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f)
            .padding(horizontal = if (style == OneUiButtonStyle.Text) 16.dp else 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, Modifier.size(20.dp), tint = content)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = OneUi.typography.button, color = content, textAlign = TextAlign.Center)
    }
}

@Composable
fun OneUiIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = OneUi.colors.text,
    background: Color = Color.Transparent,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = description, Modifier.size(iconSize), tint = tint)
    }
}

@Composable
fun OneUiFab(@DrawableRes icon: Int, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, text: String? = null) {
    val colors = OneUi.colors
    Row(
        modifier
            .height(56.dp)
            .shadow(10.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (text == null) 16.dp else 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = if (text == null) description else null, Modifier.size(24.dp), tint = colors.onAccent)
        if (text != null) {
            Spacer(Modifier.width(10.dp))
            Text(text, style = OneUi.typography.button, color = colors.onAccent)
        }
    }
}

@Composable
fun OneUiSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = OneUi.colors
    val haptics = LocalHapticFeedback.current
    val track by animateColorAsState(if (checked) colors.accent else colors.switchOff, tween(200, easing = OneUiDefaults.Easing), label = "track")
    val offset by animateDpAsState(if (checked) 21.dp else 3.dp, tween(250, easing = OneUiDefaults.Easing), label = "thumb")
    val toggle = if (onCheckedChange != null) {
        Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch) {
            haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
            onCheckedChange(it)
        }
    } else {
        Modifier
    }
    Box(modifier.then(toggle).alpha(if (enabled) 1f else 0.4f).padding(vertical = 4.dp)) {
        Box(Modifier.size(width = 40.dp, height = 22.dp).clip(CircleShape).background(track)) {
            Box(Modifier.offset(x = offset, y = 3.dp).size(16.dp).clip(CircleShape).background(Color.White))
        }
    }
}

@Composable
fun OneUiSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    divider: Boolean = false,
) {
    OneUiListRow(
        title = title,
        summary = summary,
        divider = divider,
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
        trailing = { OneUiSwitch(checked, onCheckedChange = null) },
    )
}

@Composable
fun OneUiSegmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val haptics = LocalHapticFeedback.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.component)
            .padding(4.dp)
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selected
            val background by animateColorAsState(if (isSelected) colors.container else Color.Transparent, tween(200, easing = OneUiDefaults.Easing), label = "segment")
            Box(
                Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(background)
                    .selectable(selected = isSelected, role = Role.Tab) {
                        if (!isSelected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelect(index)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    style = OneUi.typography.listSummary.copy(fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (isSelected) colors.accent else colors.secondaryText,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun OneUiChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val background by animateColorAsState(if (selected) colors.accent else colors.container, tween(200, easing = OneUiDefaults.Easing), label = "chip")
    Box(
        modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(background)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = OneUi.typography.listSummary.copy(fontWeight = FontWeight.Medium),
            color = if (selected) colors.onAccent else colors.text,
            maxLines = 1,
        )
    }
}

@Composable
fun OneUiProgress(modifier: Modifier = Modifier, size: Dp = 36.dp) {
    CircularProgressIndicator(modifier.size(size), color = OneUi.colors.accent, strokeWidth = 3.dp, trackColor = Color.Transparent)
}

@Composable
fun OneUiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = OneUi.typography.field,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    filled: Boolean = true,
) {
    val colors = OneUi.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = textStyle.copy(color = colors.text),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { field ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(if (filled) Modifier.clip(RoundedCornerShape(20.dp)).background(colors.container) else Modifier)
                    .padding(contentPadding),
            ) {
                if (value.isEmpty()) Text(placeholder, style = textStyle, color = colors.secondaryText)
                field()
            }
        },
    )
}
