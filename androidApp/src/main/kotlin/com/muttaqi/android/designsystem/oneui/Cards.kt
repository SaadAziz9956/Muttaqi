package com.muttaqi.android.designsystem.oneui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

enum class OneUiArtwork { Forest, Dawn, Lagoon }

private class Mesh(val base: Color, val points: List<Pair<Offset, Color>>)

private fun OneUiArtwork.mesh(dark: Boolean): Mesh {
    val c = when (this) {
        OneUiArtwork.Forest -> if (dark) {
            listOf(0xFF0B2A22, 0xFF114538, 0xFF1D5C4B, 0xFF14493B, 0xFF2F7D6B, 0xFF0F3A30, 0xFF1B5A4A, 0xFF3E8F84, 0xFF0E3A31)
        } else {
            listOf(0xFF114538, 0xFF185E4C, 0xFF24735F, 0xFF15503F, 0xFF2A7C67, 0xFF1F6653, 0xFF236F5C, 0xFF3A8C7A, 0xFF4E9E8E)
        }
        OneUiArtwork.Dawn -> if (dark) {
            listOf(0xFF10251F, 0xFF1A3A30, 0xFF2A3A2A, 0xFF163228, 0xFF234A3E, 0xFF2E3F30, 0xFF12302A, 0xFF1E4A40, 0xFF283828)
        } else {
            listOf(0xFFE2FFF8, 0xFFF6F1DC, 0xFFD6F2EA, 0xFFC9ECE3, 0xFFF2FBFF, 0xFFEFE6C4, 0xFFB9E4DA, 0xFFE4F6F0, 0xFFF7EFD3)
        }
        OneUiArtwork.Lagoon -> if (dark) {
            listOf(0xFF0E2A2C, 0xFF163C40, 0xFF0F2F2A, 0xFF1B4A4E, 0xFF21585A, 0xFF123A34, 0xFF0F3033, 0xFF1D4F52, 0xFF163F3A)
        } else {
            listOf(0xFFCFEFF3, 0xFF81CACF, 0xFFE2FFF8, 0xFFA9DDE0, 0xFFF2FBFF, 0xFF9BD6CC, 0xFFE0F6F7, 0xFF7FC4C4, 0xFFCDEFE6)
        }
    }.map(::Color)
    val at = listOf(Offset(0f, 0f), Offset(0.55f, 0f), Offset(1f, 0f), Offset(0f, 0.45f), Offset(1f, 0.4f), Offset(0f, 1f), Offset(0.6f, 1f), Offset(1f, 1f))
    val order = listOf(0, 1, 2, 3, 5, 6, 7, 8)
    return Mesh(c[4], at.zip(order.map { c[it] }))
}

fun OneUiArtwork.contentColor(dark: Boolean): Color = when (this) {
    OneUiArtwork.Forest -> Color.White
    OneUiArtwork.Dawn, OneUiArtwork.Lagoon -> if (dark) Color(0xFFE8F3EE) else Color(0xFF173A31)
}

fun Modifier.oneUiArtwork(artwork: OneUiArtwork, dark: Boolean): Modifier = drawBehind {
    val mesh = artwork.mesh(dark)
    drawRect(mesh.base)
    val radius = max(size.width, size.height) * 0.6f
    mesh.points.forEach { (point, color) ->
        val center = Offset(point.x * size.width, point.y * size.height)
        drawRect(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center, radius))
    }
}

@Composable
fun OneUiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    artwork: OneUiArtwork? = null,
    shape: Shape = RoundedCornerShape(OneUiDefaults.ContainerRadius),
    contentPadding: PaddingValues = PaddingValues(OneUiDefaults.ItemPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = OneUi.colors
    val dark = colors.isDark
    val surface = if (artwork != null) Modifier.oneUiArtwork(artwork, dark) else Modifier.background(colors.container)
    CompositionLocalProvider(LocalContentColor provides (artwork?.contentColor(dark) ?: colors.text)) {
        Column(
            modifier
                .clip(shape)
                .then(surface)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(contentPadding),
            content = content,
        )
    }
}

val OneUiCardSpacing: Dp = 12.dp
