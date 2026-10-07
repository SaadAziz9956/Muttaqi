package com.muttaqi.android.designsystem.oneui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal data class RecoilIndication(private val overlay: Color) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = RecoilNode(interactionSource, overlay)
}

private class RecoilNode(private val interactionSource: InteractionSource, private val overlay: Color) : Modifier.Node(), DrawModifierNode {
    private val pressed = Animatable(0f)
    private var pressJob: Job? = null

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressJob = coroutineScope.launch { pressed.animateTo(1f, tween(PRESS_MILLIS, easing = LinearEasing)) { invalidateDraw() } }
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        val press = pressJob
                        coroutineScope.launch {
                            press?.join()
                            pressed.animateTo(0f, tween(RELEASE_MILLIS, easing = ReleaseEasing)) { invalidateDraw() }
                        }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val amount = pressed.value
        val factor = 1f - (1f - PRESSED_SCALE) * amount
        scale(factor) {
            this@draw.drawContent()
            if (amount > 0f) drawRect(overlay.copy(alpha = overlay.alpha * amount))
        }
    }

    private companion object {
        const val PRESS_MILLIS = 100
        const val RELEASE_MILLIS = 350
        const val PRESSED_SCALE = 0.99f
        val ReleaseEasing = CubicBezierEasing(0.17f, 0.17f, 0.67f, 1f)
    }
}
