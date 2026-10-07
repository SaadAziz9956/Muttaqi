package com.muttaqi.android.feature.prayer

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiCard
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
    OneUiCard(modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            upcoming?.let { NextPrayer(it, Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = if (today == null) 10.dp else 4.dp), zone) }
            today?.let { PrayerTimesStrip(it, next, zone = zone) }
        }
    }
}

@Composable
fun NextPrayer(upcoming: UpcomingPrayer, modifier: Modifier = Modifier, zone: ZoneId = ZoneId.systemDefault()) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val time = upcoming.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT), zone)
    Row(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "Next prayer, ${upcoming.prayer.displayName} at $time" },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(colors.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_schedule), contentDescription = null, Modifier.size(22.dp), tint = colors.accent)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("Next prayer", style = type.caption, color = colors.secondaryText)
            Text(upcoming.prayer.displayName, style = type.listTitle.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
        }
        Text(time, style = type.listTitle.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold), color = colors.accent)
    }
}

@Composable
fun SetLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OneUiButton("Set location", onClick, modifier, icon = R.drawable.ic_location_on)
}

@Composable
fun PrayerTimesStrip(times: DailyPrayerTimes, next: Prayer?, modifier: Modifier = Modifier, zone: ZoneId = ZoneId.systemDefault()) {
    val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "hh:mm")
    val colors = OneUi.colors
    val type = OneUi.typography
    Row(modifier.fillMaxWidth()) {
        Prayer.entries.forEach { prayer ->
            val isNext = prayer == next
            Column(
                Modifier
                    .weight(1f)
                    .then(if (isNext) Modifier.background(colors.accent, RoundedCornerShape(18.dp)) else Modifier)
                    .padding(vertical = 12.dp)
                    .semantics(mergeDescendants = true) { selected = isNext },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    prayer.displayName,
                    style = type.caption,
                    color = if (isNext) colors.onAccent else colors.secondaryText,
                    maxLines = 1,
                )
                Text(
                    times.time(prayer).format(format, zone),
                    style = type.listTitle.copy(fontSize = 16.sp, fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium),
                    color = if (isNext) colors.onAccent else colors.text,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun KotlinInstant.format(formatter: DateTimeFormatter, zone: ZoneId): String =
    formatter.format(Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zone))
