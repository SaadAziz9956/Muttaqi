package com.muttaqi.android.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muttaqi.android.designsystem.MuttaqiTheme

// The soft floating style (SoftStyle.swift on iOS): a tinted canvas with soft blooms of the brand colours, cards with
// large rounded corners, a bright rim and a soft green-tinted shadow, and a springy press with a light haptic.

/** The page behind floating cards: the soft canvas with blooms of mint, teal and gold, drawn as radial gradients */
@Composable
fun SoftBackdrop(modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val blooms = if (soft.dark) {
        listOf(Color(0xFF114538), Color(0xFF0F3A3C), Color(0xFF1A2A20)).map { it.copy(alpha = 0.55f) }
    } else {
        listOf(Color(0xFFD8F3EC), Color(0xFFCFEFF3), Color(0xFFF6F1DC)).map { it.copy(alpha = 0.9f) }
    }
    Box(
        modifier
            .fillMaxSize()
            .background(soft.canvas)
            .drawBehind {
                fun bloom(color: Color, center: Offset, radius: Float) =
                    drawCircle(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center, radius), radius, center)
                bloom(blooms[0], Offset(size.width * 1.05f, size.height * 0.05f), size.width * 0.95f)
                bloom(blooms[1], Offset(-size.width * 0.15f, size.height * 1.0f), size.width * 1.05f)
                bloom(blooms[2], Offset(size.width * 0.85f, size.height * 0.7f), size.width * 0.75f)
            },
    )
}

/** Soft colour in the brand palette behind a tile's content, like light through frosted glass */
enum class SoftArtwork { Forest, Dawn, Lagoon }

internal fun SoftArtwork.brush(dark: Boolean): Brush {
    val colors = when (this) {
        SoftArtwork.Forest -> if (dark) listOf(0xFF0B2A22, 0xFF1D5C4B, 0xFF3E8F84) else listOf(0xFF114538, 0xFF24735F, 0xFF4E9E8E)
        SoftArtwork.Dawn -> if (dark) listOf(0xFF10251F, 0xFF234A3E, 0xFF2E3F30) else listOf(0xFFE2FFF8, 0xFFF2FBFF, 0xFFF7EFD3)
        SoftArtwork.Lagoon -> if (dark) listOf(0xFF0E2A2C, 0xFF21585A, 0xFF123A34) else listOf(0xFFCFEFF3, 0xFF81CACF, 0xFFE2FFF8)
    }
    return Brush.linearGradient(colors.map(::Color))
}

/** A spring press with a light haptic, like touching glass; pair with [softClickable] on the same interaction source */
@Composable
fun Modifier.softPressScale(interaction: MutableInteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "softPress",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun Modifier.softClickable(interaction: MutableInteractionSource, onClick: () -> Unit): Modifier {
    val haptics = LocalHapticFeedback.current
    return clickable(interaction, ripple()) {
        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
        onClick()
    }
}

/**
 * A floating card: a bright rim and soft shadow on the soft surface, or on [artwork]. Tappable when [onClick] is
 * given, with the soft spring press. The shadow is drawn from the card's shape, so long lists stay smooth
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    rim: Dp = 1.5.dp,
    artwork: SoftArtwork? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val soft = MuttaqiTheme.soft
    val shape = RoundedCornerShape(cornerRadius)
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .then(if (onClick != null) Modifier.softPressScale(interaction) else Modifier)
            .softFloat(shape)
            .clip(shape)
            .then(if (artwork != null) Modifier.background(artwork.brush(soft.dark)) else Modifier.background(soft.surface))
            .border(if (artwork != null) 3.dp else rim, soft.rim, shape)
            .then(if (onClick != null) Modifier.softClickable(interaction, onClick) else Modifier),
        content = content,
    )
}

/** The soft float shadow for any shape */
@Composable
fun Modifier.softFloat(shape: Shape, elevation: Dp = 14.dp): Modifier {
    val soft = MuttaqiTheme.soft
    return shadow(elevation, shape, clip = false, ambientColor = soft.shadow, spotColor = soft.shadow)
}

/** A floating capsule or circle, e.g. a chip, pill or round button, filled with [fill] */
@Composable
fun SoftPillSurface(
    modifier: Modifier = Modifier,
    fill: Color = MuttaqiTheme.soft.surface,
    shape: Shape = CircleShape,
    rim: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val soft = MuttaqiTheme.soft
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .then(if (onClick != null) Modifier.softPressScale(interaction) else Modifier)
            .softFloat(shape, elevation = 8.dp)
            .clip(shape)
            .background(fill)
            .then(if (rim) Modifier.border(1.5.dp, soft.rim, shape) else Modifier)
            .then(if (onClick != null) Modifier.softClickable(interaction, onClick) else Modifier),
        contentAlignment = androidx.compose.ui.Alignment.Center,
        content = content,
    )
}
