package com.muttaqi.android.feature.prayer

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.softFloat
import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant as KotlinInstant

/**
 * The next prayer and its time, at the top of Home; without a location there are no times, so it becomes the way to
 * set one
 */
@Composable
fun NextPrayerPill(
    upcoming: UpcomingPrayer?,
    needsLocation: Boolean,
    onSetLocation: () -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val soft = MuttaqiTheme.soft
    val style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
    when {
        upcoming != null -> {
            val time = upcoming.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT), zone)
            SoftPillSurface(
                modifier.height(34.dp).clearAndSetSemantics { contentDescription = "Next prayer, ${upcoming.prayer.displayName} at $time" },
            ) {
                Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(upcoming.prayer.displayName, style = style, color = soft.appPrimary)
                    Text(time, style = style, color = soft.appPrimary)
                }
            }
        }
        needsLocation -> SoftPillSurface(modifier.height(34.dp), onClick = onSetLocation) {
            Text("Set location", Modifier.padding(horizontal = 14.dp), style = style, color = soft.appPrimary)
        }
    }
}

/** Today's five prayers, the next one picked out in a green capsule */
@Composable
fun PrayerTimesStrip(times: DailyPrayerTimes, next: Prayer?, modifier: Modifier = Modifier, zone: ZoneId = ZoneId.systemDefault()) {
    val soft = MuttaqiTheme.soft
    // The hour and minute without AM or PM, as iOS draws them: two digits, in 12 or 24 hours as the phone is set
    val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "hh:mm")
    SoftCard(modifier, cornerRadius = 30.dp) {
        Row(Modifier.padding(6.dp)) {
            Prayer.entries.forEach { prayer ->
                val isNext = prayer == next
                Column(
                    Modifier.weight(1f)
                        .then(if (isNext) Modifier.softFloat(CircleShape, elevation = 6.dp).background(soft.brandGreen, CircleShape) else Modifier)
                        .padding(vertical = 10.dp)
                        .semantics(mergeDescendants = true) { selected = isNext },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        prayer.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isNext) Color.White.copy(alpha = 0.85f) else soft.textSecondary,
                    )
                    Text(
                        times.time(prayer).format(format, zone),
                        // Figures of one width, so the times line up as they change
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, fontFeatureSettings = "tnum"),
                        color = if (isNext) Color.White else soft.appPrimary,
                    )
                }
            }
        }
    }
}

private fun KotlinInstant.format(formatter: DateTimeFormatter, zone: ZoneId): String =
    formatter.format(Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zone))
