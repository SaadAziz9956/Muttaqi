package com.muttaqi.android.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.feature.topics.topicIcon
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeState
import java.text.NumberFormat
import kotlin.math.roundToInt

private val Spacing = 14.dp
private val TileHeight = 132.dp

@Composable
internal fun HomeBento(state: HomeState, qiblaArrow: () -> Double?, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing)) {
            QiblaTile(state, qiblaArrow, Modifier.weight(1f).height(TileHeight * 2 + Spacing)) { onIntent(HomeIntent.QiblaTapped) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing)) {
                DhikrTile(state.dhikrToday, Modifier.height(TileHeight)) { onIntent(HomeIntent.DhikrTapped) }
                NameTile(state, Modifier.height(TileHeight)) { onIntent(HomeIntent.NameTapped) }
            }
        }
        JournalTile(state, onIntent, Modifier.heightIn(min = 96.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing)) {
            EmotionsTile(Modifier.weight(1f).height(TileHeight)) { onIntent(HomeIntent.EmotionsTapped) }
            TopicTile(state, Modifier.weight(1f).height(TileHeight)) { onIntent(HomeIntent.TopicTapped) }
        }
    }
}

@Composable
private fun QiblaTile(state: HomeState, qiblaArrow: () -> Double?, modifier: Modifier, onClick: () -> Unit) {
    val qibla = state.qibla
    val bearing = qibla?.bearing?.roundToInt()
    Tile(
        label = "Qibla",
        icon = R.drawable.ic_home_qibla,
        artwork = SoftArtwork.Forest,
        onClick = onClick,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = if (bearing != null) "Qibla, $bearing degrees" else "Qibla"
            role = Role.Button
            onClick { onClick(); true }
        },
    ) {
        Spacer(Modifier.weight(1f))
        QiblaPointer(qiblaArrow, qibla?.bearing, Modifier.size(112.dp).align(Alignment.CenterHorizontally))
        Spacer(Modifier.weight(1f))
        if (qibla != null) {
            Text("$bearing°", style = MaterialTheme.typography.labelLarge.copy(fontSize = 30.sp), color = Color.White)
            Text(
                "Makkah · ${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km",
                Modifier.alpha(0.8f),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        } else {
            Text("Find the Qibla", style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp), color = Color.White)
        }
    }
}

@Composable
private fun QiblaPointer(qiblaArrow: () -> Double?, bearing: Double?, modifier: Modifier = Modifier) {
    val arrow = qiblaArrow()
    val target = ((arrow ?: bearing ?: 0.0) - 45).toFloat()
    val rotation by animateFloatAsState(target, tween(250), label = "qiblaArrow")
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape))
        Box(Modifier.fillMaxSize().padding(10.dp).background(Color.White.copy(alpha = 0.12f), CircleShape))
        val turned = Modifier.size(42.dp).graphicsLayer { rotationZ = if (arrow != null) rotation else target }
        Icon(
            painterResource(R.drawable.ic_send_2_bold),
            null,
            turned.offset(y = 3.dp).blur(6.dp),
            tint = Color.Black.copy(alpha = 0.2f),
        )
        Icon(painterResource(R.drawable.ic_send_2_bold), null, turned, tint = Color.White)
    }
}

@Composable
private fun DhikrTile(said: Int, modifier: Modifier, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Tile(label = "Dikr", icon = R.drawable.ic_repeat_circle_linear, onClick = onClick, modifier = modifier) {
        Spacer(Modifier.weight(1f))
        if (said > 0) {
            Text(NumberFormat.getIntegerInstance().format(said), style = MaterialTheme.typography.labelLarge.copy(fontSize = 30.sp), color = soft.appPrimary)
            Text("said today", Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
        } else {
            Text("Begin today's dhikr", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp), color = soft.appPrimary)
        }
    }
}

@Composable
private fun NameTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Tile(label = "Name of the day", icon = null, artwork = SoftArtwork.Dawn, onClick = onClick, modifier = modifier) {
        state.nameOfTheDay?.let { name ->
            Spacer(Modifier.weight(1f))
            QuranText(
                name.arabic,
                Modifier.fillMaxWidth(),
                fontSize = 26.sp,
                color = soft.appPrimary,
                textAlign = TextAlign.Right,
                lineSpacing = 0.sp,
            )
            Text(
                name.transliteration,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = soft.appPrimary,
            )
        }
    }
}

@Composable
private fun JournalTile(state: HomeState, onIntent: (HomeIntent) -> Unit, modifier: Modifier) {
    val soft = MuttaqiTheme.soft
    val entry = state.journalToday
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SoftCard(
            Modifier.fillMaxWidth().semantics { contentDescription = "Journal" },
            onClick = { onIntent(HomeIntent.JournalTapped) },
        ) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TileLabel("Journal", R.drawable.ic_book_linear, soft.textSecondary)
                    Text(
                        entry?.preview ?: "What are you grateful for today?",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                        color = soft.appPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(14.dp))
                SoftIconButton(
                    if (entry == null) R.drawable.ic_add_linear else R.drawable.ic_arrow_right_01_linear,
                    if (entry == null) "New entry" else "Today's entry",
                    { onIntent(HomeIntent.TodaysEntryTapped) },
                    size = 44.dp,
                    iconSize = 20.dp,
                    filled = true,
                )
            }
        }
    }
}

@Composable
private fun EmotionsTile(modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Emotions", icon = R.drawable.ic_happyemoji_linear, artwork = SoftArtwork.Lagoon, onClick = onClick, modifier = modifier) {
        Spacer(Modifier.weight(1f))
        Text("How do you feel?", style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp), color = MuttaqiTheme.soft.appPrimary)
    }
}

@Composable
private fun TopicTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Tile(label = "Topic of the day", icon = null, onClick = onClick, modifier = modifier) {
        state.topicOfTheDay?.let { topic ->
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp).background(soft.tintedSurface, CircleShape), contentAlignment = Alignment.Center) {
                Icon(painterResource(topicIcon(topic.icon)), null, Modifier.size(22.dp), tint = soft.brandTeal)
            }
            Spacer(Modifier.weight(1f))
            Text(
                topic.title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                color = soft.appPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Tile(
    label: String,
    @DrawableRes icon: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: SoftArtwork? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val onArtwork = artwork == SoftArtwork.Forest
    SoftCard(modifier, rim = 3.dp, artwork = artwork, onClick = onClick) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                TileLabel(label, icon, if (onArtwork) Color.White.copy(alpha = 0.85f) else MuttaqiTheme.soft.textSecondary, Modifier.weight(1f))
                Spacer(Modifier.width(4.dp))
                SoftPillSurface(Modifier.size(30.dp), fill = MuttaqiTheme.soft.surface.copy(alpha = 1f)) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_right_01_linear),
                        null,
                        Modifier.size(14.dp).rotate(-45f),
                        tint = MuttaqiTheme.soft.appPrimary,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun TileLabel(text: String, @DrawableRes icon: Int?, tint: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Icon(painterResource(icon), null, Modifier.size(15.dp), tint = tint)
        Text(text, style = MaterialTheme.typography.bodySmall, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
