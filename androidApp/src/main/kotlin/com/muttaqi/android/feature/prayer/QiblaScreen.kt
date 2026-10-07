package com.muttaqi.android.feature.prayer

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.FadeBetween
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaCompass
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaEffect
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaIntent
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaPhase
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaState
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import java.text.NumberFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaRoute(viewModel: QiblaViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val compass by viewModel.compass.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                QiblaEffect.FacingQibla -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                QiblaEffect.OpenSettings -> context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            }
        }
    }
    QiblaScreen(state, compass, viewModel::dispatch, onBack)
}

@Composable
fun QiblaScreen(state: QiblaState, compass: QiblaCompass?, onIntent: (QiblaIntent) -> Unit, onBack: () -> Unit) {
    OneUiSurface {
        OneUiScaffold(title = "Qibla", onBack = onBack, expandable = false) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                FadeBetween(state.phase, key = { it::class }, contentAlignment = Alignment.Center) { phase ->
                    when (phase) {
                        QiblaPhase.Locating -> DelayedLoadingIndicator()
                        is QiblaPhase.NeedsLocation -> LocationNeeded(phase.access) { onIntent(QiblaIntent.LocationButtonTapped) }
                        is QiblaPhase.Ready -> Compass(phase.qibla, compass, state.isCompassAvailable)
                    }
                }
            }
        }
    }
}

@Composable
private fun Compass(qibla: QiblaDirection, compass: QiblaCompass?, isCompassAvailable: Boolean) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val bearing = qibla.bearing.roundToInt()
    val isAligned = compass?.isAligned == true
    val note = when {
        !isCompassAvailable -> "This device has no compass. Use one to face $bearing° from North."
        compass?.needsCalibration == true -> "Move your phone in a figure-eight to calibrate the compass."
        else -> null
    }
    val instruction = when {
        compass == null -> "Face $bearing° from North"
        isAligned -> "You're facing the Qibla"
        else -> "Turn ${if (compass.turnAngle > 0) "right" else "left"} ${abs(compass.turnAngle).roundToInt()}°"
    }
    val instructionColor by animateColorAsState(if (isAligned) colors.accent else colors.text, spring(), label = "instruction")
    Column(
        Modifier.fillMaxSize().padding(horizontal = OneUiDefaults.ScreenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
        QiblaDial(qibla.bearing, rotation = compass?.dialRotation ?: 0.0, isAligned = isAligned, modifier = Modifier.size(300.dp))
        Text(
            instruction,
            Modifier.padding(top = 32.dp, start = 12.dp, end = 12.dp),
            style = type.sectionTitle.copy(fontSize = 24.sp, lineHeight = 32.sp),
            color = instructionColor,
            textAlign = TextAlign.Center,
        )
        Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
            Stat("Bearing", "$bearing°", "from North", Modifier.weight(1f))
            Stat("Distance", "${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km", "to Makkah", Modifier.weight(1f))
        }
        if (note != null) {
            OneUiCard(Modifier.padding(top = OneUiCardSpacing).fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_info), contentDescription = null, Modifier.size(22.dp), tint = colors.accent)
                    Text(note, style = type.listSummary, color = colors.secondaryText)
                }
            }
        }
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val type = OneUi.typography
    OneUiCard(modifier) {
        Column(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = type.caption, color = colors.secondaryText)
            Text(value, style = type.listTitle.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold), color = colors.text)
            Text(detail, style = type.small, color = colors.accent)
        }
    }
}

private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

@Composable
fun QiblaDial(qiblaBearing: Double, rotation: Double, isAligned: Boolean, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val turned by animateFloatAsState(rotation.toFloat(), tween(250, easing = EaseOut), label = "dial")
    val accent by animateColorAsState(if (isAligned) colors.accent else colors.text, spring(), label = "accent")
    val onAccent by animateColorAsState(if (isAligned) colors.onAccent else colors.container, spring(), label = "onAccent")
    val pointer by animateColorAsState(if (isAligned) colors.accent else colors.secondaryText, spring(), label = "pointer")
    val rim by animateColorAsState(if (isAligned) colors.accent else colors.divider, spring(), label = "rim")
    val rimWidth by animateDpAsState(if (isAligned) 4.dp else 1.dp, spring(), label = "rimWidth")
    val letters = listOf("N", "E", "S", "W")

    Box(modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            drawCircle(colors.container, radius)
            drawCircle(rim, radius - rimWidth.toPx() / 2, style = Stroke(rimWidth.toPx()))
        }

        Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -turned }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                drawCircle(colors.component, radius - 22.dp.toPx())
                for (tick in 0 until 72) {
                    val major = tick % 6 == 0
                    rotate(tick * 5f) {
                        capsule(
                            color = if (major) colors.text else colors.secondaryText.copy(alpha = 0.6f),
                            width = if (major) 2.dp.toPx() else 1.dp.toPx(),
                            height = if (major) 12.dp.toPx() else 6.dp.toPx(),
                            centreAbove = radius - 12.dp.toPx(),
                        )
                    }
                }
                val needle = radius - 82.dp.toPx()
                rotate(qiblaBearing.toFloat()) { capsule(accent, 3.dp.toPx(), needle, centreAbove = needle / 2) }
            }
            letters.forEachIndexed { index, letter ->
                Text(
                    letter,
                    Modifier.polar(index * 90.0, fromCentre = 150.dp - 44.dp).graphicsLayer { rotationZ = turned },
                    style = type.listTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = if (letter == "N") colors.accent else colors.secondaryText,
                )
            }
            Box(
                Modifier.polar(qiblaBearing, fromCentre = 150.dp - 82.dp)
                    .graphicsLayer { rotationZ = turned }
                    .size(44.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_mosque), contentDescription = null, Modifier.size(22.dp), tint = onAccent)
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            capsule(pointer, 4.dp.toPx(), 18.dp.toPx(), centreAbove = radius + 16.dp.toPx())
            drawCircle(accent, 7.dp.toPx())
            drawCircle(colors.container, 6.dp.toPx(), style = Stroke(2.dp.toPx()))
        }
    }
}

private fun DrawScope.capsule(color: Color, width: Float, height: Float, centreAbove: Float) {
    drawRoundRect(
        color,
        topLeft = Offset(center.x - width / 2, center.y - centreAbove - height / 2),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2),
    )
}

private fun Modifier.polar(degrees: Double, fromCentre: Dp): Modifier = offset {
    val distance = fromCentre.toPx()
    val radians = degrees * PI / 180
    IntOffset((sin(radians) * distance).roundToInt(), (-cos(radians) * distance).roundToInt())
}

@Composable
private fun LocationNeeded(access: LocationAccess, onButton: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(
        Modifier.padding(horizontal = 32.dp).semantics(mergeDescendants = true) { contentDescription = "Location needed" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(80.dp).background(colors.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_location_on), contentDescription = null, Modifier.size(40.dp), tint = colors.accent)
        }
        Text("Location needed", Modifier.padding(top = 20.dp), style = type.sectionTitle.copy(fontSize = 24.sp, lineHeight = 32.sp), color = colors.text)
        Text(
            "Muttaqi uses your location to find the direction of the Kaaba.",
            Modifier.padding(top = 8.dp),
            style = type.body,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
        OneUiButton(
            if (access == LocationAccess.Denied) "Open Settings" else "Allow Location",
            onClick = onButton,
            modifier = Modifier.padding(top = 28.dp),
        )
    }
}
