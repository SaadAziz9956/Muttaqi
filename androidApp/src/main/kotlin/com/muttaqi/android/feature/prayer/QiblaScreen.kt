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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.FadeBetween
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QiblaScreen(state: QiblaState, compass: QiblaCompass?, onIntent: (QiblaIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Qibla") }, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
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

@Composable
private fun Compass(qibla: QiblaDirection, compass: QiblaCompass?, isCompassAvailable: Boolean) {
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
    val instructionColor by animateColorAsState(
        if (isAligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        spring(),
        label = "instruction",
    )
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
        QiblaDial(qibla.bearing, rotation = compass?.dialRotation ?: 0.0, isAligned = isAligned, modifier = Modifier.size(300.dp))
        Text(
            instruction,
            Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = instructionColor,
            textAlign = TextAlign.Center,
        )
        Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("Bearing", "$bearing°", "from North", Modifier.weight(1f))
            Stat("Distance", "${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km", "to Makkah", Modifier.weight(1f))
        }
        if (note != null) {
            Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.fillMaxWidth().padding(16.dp).semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

@Composable
fun QiblaDial(qiblaBearing: Double, rotation: Double, isAligned: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val turned by animateFloatAsState(rotation.toFloat(), tween(250, easing = EaseOut), label = "dial")
    val accent by animateColorAsState(if (isAligned) colors.primary else colors.tertiary, spring(), label = "accent")
    val onAccent by animateColorAsState(if (isAligned) colors.onPrimary else colors.onTertiary, spring(), label = "onAccent")
    val pointer by animateColorAsState(if (isAligned) colors.primary else colors.onSurfaceVariant, spring(), label = "pointer")
    val rim by animateColorAsState(if (isAligned) colors.primary else colors.outlineVariant, spring(), label = "rim")
    val rimWidth by animateDpAsState(if (isAligned) 4.dp else 1.dp, spring(), label = "rimWidth")
    val markerShape = MaterialShapes.Cookie9Sided.toShape()
    val letters = listOf("N", "E", "S", "W")

    Box(modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            drawCircle(colors.surfaceContainer, radius)
            drawCircle(rim, radius - rimWidth.toPx() / 2, style = Stroke(rimWidth.toPx()))
        }

        Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -turned }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                drawCircle(colors.surfaceContainerHighest, radius - 22.dp.toPx())
                for (tick in 0 until 72) {
                    val major = tick % 6 == 0
                    rotate(tick * 5f) {
                        capsule(
                            color = if (major) colors.onSurface else colors.outline,
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
                    style = MaterialTheme.typography.titleMedium,
                    color = if (letter == "N") colors.primary else colors.onSurfaceVariant,
                )
            }
            Box(
                Modifier.polar(qiblaBearing, fromCentre = 150.dp - 82.dp)
                    .graphicsLayer { rotationZ = turned }
                    .size(44.dp)
                    .background(accent, markerShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_mosque), contentDescription = null, Modifier.size(22.dp), tint = onAccent)
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            capsule(pointer, 4.dp.toPx(), 18.dp.toPx(), centreAbove = radius + 16.dp.toPx())
            drawCircle(accent, 7.dp.toPx())
            drawCircle(colors.surface, 6.dp.toPx(), style = Stroke(2.dp.toPx()))
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
    Column(
        Modifier.padding(horizontal = 32.dp).semantics(mergeDescendants = true) { contentDescription = "Location needed" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(R.drawable.ic_location_on), contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Location needed", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.headlineSmall)
        Text(
            "Muttaqi uses your location to find the direction of the Kaaba.",
            Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onButton, modifier = Modifier.padding(top = 24.dp)) {
            Text(if (access == LocationAccess.Denied) "Open Settings" else "Allow Location")
        }
    }
}
