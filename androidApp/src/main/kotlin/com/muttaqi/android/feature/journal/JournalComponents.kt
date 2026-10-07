package com.muttaqi.android.feature.journal

import android.text.format.DateFormat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiDialog
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

@Composable
internal fun DeleteEntryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    OneUiDialog(
        title = "Delete this entry?",
        text = "This can't be undone.",
        onDismiss = onDismiss,
        confirmText = "Delete Entry",
        onConfirm = onConfirm,
        destructive = true,
    )
}

internal fun groupItemShape(index: Int, count: Int): Shape {
    val radius = OneUiDefaults.ContainerRadius
    val top = if (index == 0) radius else 0.dp
    val bottom = if (index == count - 1) radius else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

private fun Instant.local(): ZonedDateTime = java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(ZoneId.systemDefault())

private fun Instant.format(skeleton: String): String {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(local())
}

internal fun Instant.dayAndMonth(): String = format("dMMM")

internal fun Instant.weekdayAndYear(): String {
    val weekday = format("EEEE")
    return if (local().year == ZonedDateTime.now().year) weekday else "$weekday · ${format("y")}"
}

internal fun Instant.longDate(): String = format("dMMMMy")

internal fun Instant.weekday(): String = format("EEEE")
