package com.muttaqi.android.feature.journal

import android.text.format.DateFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

@Composable
internal fun DeleteEntryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this entry?") },
        text = { Text("This can't be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete Entry", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
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

internal fun Instant.entryDate(): String =
    DateTimeFormatter.ofPattern("dd - MMM - y", Locale.getDefault()).format(local())
