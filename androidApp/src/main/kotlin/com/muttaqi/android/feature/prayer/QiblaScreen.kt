package com.muttaqi.android.feature.prayer

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.brush
import com.muttaqi.android.designsystem.component.softFloat
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QiblaScreen(state: QiblaState, compass: QiblaCompass?, onIntent: (QiblaIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Box {
        SoftBackdrop()
        Scaffold(containerColor = Color.Transparent, topBar = { QiblaTopBar(onBack) }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                when (val phase = state.phase) {
                    QiblaPhase.Locating -> LoadingIndicator(color = soft.appPrimary)
                    is QiblaPhase.NeedsLocation -> LocationNeeded(phase.access) { onIntent(QiblaIntent.LocationButtonTapped) }
                    is QiblaPhase.Ready -> Compass(phase.qibla, compass, state.isCompassAvailable)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QiblaTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Qibla", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp), color = MuttaqiTheme.soft.appPrimary) },
        navigationIcon = {
            SoftIconButton(R.drawable.ic_arrow_left_02_linear, "Back", onBack, Modifier.padding(start = 12.dp), size = 44.dp, iconSize = 22.dp)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
    )
}

@Composable
private fun Compass(qibla: QiblaDirection, compass: QiblaCompass?, isCompassAvailable: Boolean) {
    val soft = MuttaqiTheme.soft
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
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
        QiblaDial(qibla.bearing, rotation = compass?.dialRotation ?: 0.0, isAligned = isAligned, modifier = Modifier.size(300.dp))
        Text(
            instruction,
            Modifier.padding(top = 36.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp),
            color = soft.appPrimary,
        )
        Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Stat("Bearing", "$bearing°", "from North", Modifier.weight(1f))
            Stat("Distance", "${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km", "to Makkah", Modifier.weight(1f))
        }
        if (note != null) {
            SoftCard(Modifier.padding(top = 14.dp).fillMaxWidth(), cornerRadius = 22.dp) {
                Text(
                    note,
                    Modifier.padding(16.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = soft.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.heightIn(min = 12.dp).weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    SoftCard(modifier, cornerRadius = 24.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp).semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
            Text(value, style = MaterialTheme.typography.labelLarge.copy(fontSize = 22.sp), color = soft.appPrimary)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
        }
    }
}

private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

@Composable
fun QiblaDial(qiblaBearing: Double, rotation: Double, isAligned: Boolean, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val green = soft.brandGreen
    val turned by animateFloatAsState(rotation.toFloat(), tween(250, easing = EaseOut), label = "dial")
    val alignedAlpha by animateFloatAsState(if (isAligned) 1f else 0f, spring(), label = "aligned")
    val accent by animateColorAsState(if (isAligned) green else soft.brandTeal, spring(), label = "accent")
    val pointer by animateColorAsState(if (isAligned) green else soft.textSecondary, spring(), label = "pointer")
    val letters = listOf("N", "E", "S", "W")

    Box(modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().softFloat(CircleShape, elevation = 16.dp).background(soft.surface, CircleShape).border(2.dp, soft.rim, CircleShape))
        Box(
            Modifier.fillMaxSize().alpha(alignedAlpha)
                .shadow(12.dp, CircleShape, clip = false, ambientColor = green.copy(alpha = 0.45f), spotColor = green.copy(alpha = 0.45f))
                .border(3.dp, green, CircleShape),
        )

        Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -turned }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                drawCircle(soft.tintedSurface, radius - 22.dp.toPx())
                for (tick in 0 until 72) {
                    val major = tick % 6 == 0
                    rotate(tick * 5f) {
                        capsule(
                            color = if (major) soft.appPrimary else soft.textSecondary.copy(alpha = 0.45f),
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
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                    color = if (letter == "N") soft.appPrimary else soft.textSecondary,
                )
            }
            Box(
                Modifier.polar(qiblaBearing, fromCentre = 150.dp - 82.dp)
                    .graphicsLayer { rotationZ = turned }
                    .size(42.dp)
                    .softFloat(CircleShape, elevation = 6.dp)
                    .background(SoftArtwork.Forest.brush(soft.dark), CircleShape)
                    .border(2.dp, soft.rim, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_home_qibla), null, Modifier.size(20.dp), tint = Color.White)
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            capsule(pointer, 4.dp.toPx(), 18.dp.toPx(), centreAbove = radius + 16.dp.toPx())
            drawCircle(accent, 7.dp.toPx())
            drawCircle(soft.rim, 6.dp.toPx(), style = Stroke(2.dp.toPx()))
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
    val soft = MuttaqiTheme.soft
    val title = if (soft.dark) Color.White else Color.Black
    val description = if (soft.dark) Color(0x99EBEBF5) else Color(0x993C3C43)
    Column(
        Modifier.padding(horizontal = 32.dp).semantics(mergeDescendants = true) { contentDescription = "Location needed" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(R.drawable.ic_home_qibla), null, Modifier.size(44.dp), tint = soft.brandTeal)
        Text(
            "Location needed",
            Modifier.padding(top = 18.dp),
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = title,
        )
        Text(
            "Muttaqi uses your location to find the direction of the Kaaba.",
            Modifier.padding(top = 8.dp),
            fontFamily = FontFamily.Default,
            fontSize = 17.sp,
            lineHeight = 25.sp,
            color = description,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onButton,
            modifier = Modifier.padding(top = 18.dp).height(34.dp),
            colors = ButtonDefaults.buttonColors(containerColor = soft.brandGreen, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 11.dp),
        ) {
            Text(
                if (access == LocationAccess.Denied) "Open Settings" else "Allow Location",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
            )
        }
    }
}
