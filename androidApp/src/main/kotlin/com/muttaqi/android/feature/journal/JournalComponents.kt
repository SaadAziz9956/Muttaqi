package com.muttaqi.android.feature.journal

import android.text.format.DateFormat
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftPillSurface
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

// The journal is a plain page like Notes, as on iOS: the system background rather than the soft backdrop, and its
// bar buttons in the text colour

/** The page behind the journal: white, or black in dark mode, as iOS's system background */
internal val journalBackground: Color
    @Composable @ReadOnlyComposable get() = if (MuttaqiTheme.soft.dark) Color.Black else Color.White

/** The hairline between entries, as iOS's list separator */
internal val journalDivider: Color
    @Composable @ReadOnlyComposable get() = if (MuttaqiTheme.soft.dark) Color(0xFF38383A) else Color(0xFFE3E3E6)

/** iOS's red for deleting */
internal val destructiveRed: Color
    @Composable @ReadOnlyComposable get() = if (MuttaqiTheme.soft.dark) Color(0xFFFF453A) else Color(0xFFFF3B30)

/** A round floating bar button with an Iconsax icon */
@Composable
internal fun JournalBarButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MuttaqiTheme.soft.textPrimary,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
) {
    SoftPillSurface(modifier.size(size), onClick = onClick) {
        Icon(painterResource(icon), contentDescription, Modifier.size(iconSize), tint = tint)
    }
}

/** The bar over a journal page: the back button, the page's title once its own has scrolled away, and [trailing] */
@Composable
internal fun JournalTopBar(
    title: String,
    showTitle: Boolean,
    onBack: () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    val soft = MuttaqiTheme.soft
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 16.dp)) {
        JournalBarButton(R.drawable.ic_arrow_left_02_linear, "Back", onBack, Modifier.align(Alignment.CenterStart))
        AnimatedVisibility(showTitle, Modifier.align(Alignment.Center), enter = fadeIn(), exit = fadeOut()) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = soft.appPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
    }
}

private fun Instant.local(): ZonedDateTime = java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(ZoneId.systemDefault())

private fun Instant.format(skeleton: String): String {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(local())
}

/** "30 Sep", in the reader's order for day and month */
internal fun Instant.dayAndMonth(): String = format("dMMM")

/** The weekday, plus the year for entries from another year, since the date beside it shows only day and month */
internal fun Instant.weekdayAndYear(): String {
    val weekday = format("EEEE")
    return if (local().year == ZonedDateTime.now().year) weekday else "$weekday · ${format("y")}"
}

/** "02 - May - 2023", as in the design */
internal fun Instant.entryDate(): String =
    DateTimeFormatter.ofPattern("dd - MMM - y", Locale.getDefault()).format(local())
