package com.muttaqi.android.designsystem.oneui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class OneUiTab(val label: String, @DrawableRes val icon: Int, @DrawableRes val selectedIcon: Int)

val LocalTabBarInset = staticCompositionLocalOf { 0.dp }

val TabBarInset: Dp = 54.dp + 24.dp

@Composable
fun OneUiTabBar(tabs: List<OneUiTab>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val haptics = LocalHapticFeedback.current
    Row(
        modifier
            .padding(bottom = 12.dp)
            .widthIn(max = 430.dp)
            .shadow(16.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.2f), spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(CircleShape)
            .background(colors.tabBar)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.Center,
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selected
            val background by animateColorAsState(if (isSelected) colors.tabSelected else Color.Transparent, tween(200, easing = OneUiDefaults.Easing), label = "tab")
            Column(
                Modifier
                    .height(46.dp)
                    .clip(CircleShape)
                    .background(background)
                    .selectable(selected = isSelected, role = Role.Tab) {
                        if (!isSelected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelect(index)
                    }
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painterResource(if (isSelected) tab.selectedIcon else tab.icon),
                    contentDescription = null,
                    Modifier.size(22.dp),
                    tint = if (isSelected) colors.accent else colors.text.copy(alpha = 0.6f),
                )
                Text(
                    tab.label,
                    style = OneUi.typography.tabLabel.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (isSelected) colors.text else colors.text.copy(alpha = 0.6f),
                    maxLines = 1,
                )
            }
        }
    }
}
