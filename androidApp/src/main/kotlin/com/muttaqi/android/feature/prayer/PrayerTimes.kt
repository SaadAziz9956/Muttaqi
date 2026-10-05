package com.muttaqi.android.feature.prayer

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant as KotlinInstant

@Composable
fun PrayerTimesCard(
    upcoming: UpcomingPrayer?,
    today: DailyPrayerTimes?,
    next: Prayer?,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            upcoming?.let { NextPrayer(it, Modifier.padding(8.dp), zone) }
            today?.let { PrayerTimesStrip(it, next, zone = zone) }
        }
    }
}

@Composable
fun NextPrayer(upcoming: UpcomingPrayer, modifier: Modifier = Modifier, zone: ZoneId = ZoneId.systemDefault()) {
    val time = upcoming.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT), zone)
    Row(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "Next prayer, ${upcoming.prayer.displayName} at $time" },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_schedule), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text("Next prayer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(upcoming.prayer.displayName, style = MaterialTheme.typography.titleMedium)
        }
        Text(time, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SetLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
        Icon(painterResource(R.drawable.ic_location_on), contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Set location")
    }
}

@Composable
fun PrayerTimesStrip(times: DailyPrayerTimes, next: Prayer?, modifier: Modifier = Modifier, zone: ZoneId = ZoneId.systemDefault()) {
    val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "hh:mm")
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth()) {
        Prayer.entries.forEach { prayer ->
            val isNext = prayer == next
            Column(
                Modifier
                    .weight(1f)
                    .then(if (isNext) Modifier.background(colors.primary, MaterialTheme.shapes.large) else Modifier)
                    .padding(vertical = 12.dp)
                    .semantics(mergeDescendants = true) { selected = isNext },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    prayer.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isNext) colors.onPrimary else colors.onSurfaceVariant,
                )
                Text(
                    times.time(prayer).format(format, zone),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isNext) colors.onPrimary else colors.onSurface,
                )
            }
        }
    }
}

private fun KotlinInstant.format(formatter: DateTimeFormatter, zone: ZoneId): String =
    formatter.format(Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zone))
