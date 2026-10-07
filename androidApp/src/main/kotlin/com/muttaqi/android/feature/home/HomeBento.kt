package com.muttaqi.android.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiArtwork
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.topicSymbol
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeState
import java.text.NumberFormat
import kotlin.math.roundToInt

private val TileMinHeight = 128.dp
private val TilePadding = 18.dp

@Composable
internal fun HomeTiles(state: HomeState, qiblaArrow: () -> Double?, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
            QiblaTile(state, qiblaArrow, Modifier.weight(1f)) { onIntent(HomeIntent.QiblaTapped) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
                DhikrTile(state.dhikrToday, Modifier.fillMaxWidth()) { onIntent(HomeIntent.DhikrTapped) }
                NameTile(state, Modifier.fillMaxWidth()) { onIntent(HomeIntent.NameTapped) }
            }
        }
        JournalTile(state, onIntent)
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(OneUiCardSpacing)) {
            EmotionsTile(Modifier.weight(1f)) { onIntent(HomeIntent.EmotionsTapped) }
            TopicTile(state, Modifier.weight(1f)) { onIntent(HomeIntent.TopicTapped) }
        }
    }
}

@Composable
private fun bigNumber(): TextStyle = OneUi.typography.listTitle.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)

@Composable
private fun tileText(): TextStyle = OneUi.typography.listTitle.copy(fontWeight = FontWeight.Medium)

@Composable
private fun QiblaTile(state: HomeState, qiblaArrow: () -> Double?, modifier: Modifier, onClick: () -> Unit) {
    val qibla = state.qibla
    val bearing = qibla?.bearing?.roundToInt()
    Tile(
        label = "Qibla",
        icon = R.drawable.ic_explore,
        onClick = onClick,
        artwork = OneUiArtwork.Forest,
        fillHeight = true,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = if (bearing != null) "Qibla, $bearing degrees" else "Qibla"
            role = Role.Button
            onClick { onClick(); true }
        },
    ) {
        val content = LocalContentColor.current
        QiblaPointer(qiblaArrow, qibla?.bearing, Modifier.padding(vertical = 16.dp).size(104.dp).align(Alignment.CenterHorizontally))
        Column {
            if (qibla != null) {
                Text("$bearing°", style = bigNumber(), color = content)
                Text(
                    "Makkah · ${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km",
                    style = OneUi.typography.listSummary,
                    color = content.copy(alpha = 0.8f),
                )
            } else {
                Text("Find the Qibla", style = tileText(), color = content)
            }
        }
    }
}

@Composable
private fun QiblaPointer(qiblaArrow: () -> Double?, bearing: Double?, modifier: Modifier = Modifier) {
    val arrow = qiblaArrow()
    val target = (arrow ?: bearing ?: 0.0).toFloat()
    val rotation by animateFloatAsState(target, tween(250), label = "qiblaArrow")
    val content = LocalContentColor.current
    Box(modifier.background(content.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(R.drawable.ic_navigation_filled),
            contentDescription = null,
            Modifier.size(46.dp).graphicsLayer { rotationZ = if (arrow != null) rotation else target },
            tint = content,
        )
    }
}

@Composable
private fun DhikrTile(said: Int, modifier: Modifier, onClick: () -> Unit) {
    val colors = OneUi.colors
    Tile(label = "Dikr", icon = R.drawable.ic_self_improvement, onClick = onClick, modifier = modifier) {
        if (said > 0) {
            Column {
                Text(NumberFormat.getIntegerInstance().format(said), style = bigNumber(), color = colors.accent)
                Text("said today", style = OneUi.typography.listSummary, color = colors.secondaryText)
            }
        } else {
            Text("Begin today's dhikr", style = tileText(), color = colors.accent)
        }
    }
}

@Composable
private fun NameTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Name of the day", icon = null, onClick = onClick, modifier = modifier, artwork = OneUiArtwork.Dawn) {
        state.nameOfTheDay?.let { name ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ArabicText(
                    name.arabic,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = OneUi.colors.accent,
                )
                Text(name.transliteration, style = tileText(), color = LocalContentColor.current)
            }
        }
    }
}

@Composable
private fun JournalTile(state: HomeState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    val entry = state.journalToday
    OneUiCard(
        modifier = modifier.fillMaxWidth().semantics { contentDescription = "Journal" },
        onClick = { onIntent(HomeIntent.JournalTapped) },
        contentPadding = PaddingValues(start = TilePadding, end = 14.dp, top = TilePadding, bottom = TilePadding),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TileLabel("Journal", R.drawable.ic_edit_note, colors.secondaryText, colors.accent)
                Text(
                    entry?.preview ?: "What are you grateful for today?",
                    style = tileText(),
                    color = colors.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OneUiIconButton(
                if (entry == null) R.drawable.ic_add else R.drawable.ic_arrow_forward,
                if (entry == null) "New entry" else "Today's entry",
                onClick = { onIntent(HomeIntent.TodaysEntryTapped) },
                tint = colors.onAccent,
                background = colors.accent,
            )
        }
    }
}

@Composable
private fun EmotionsTile(modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Emotions", icon = R.drawable.ic_mood, onClick = onClick, modifier = modifier, artwork = OneUiArtwork.Lagoon, fillHeight = true) {
        Text("How do you feel?", style = tileText(), color = OneUi.colors.accent)
    }
}

@Composable
private fun TopicTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    val colors = OneUi.colors
    Tile(label = "Topic of the day", icon = null, onClick = onClick, modifier = modifier, fillHeight = true) {
        state.topicOfTheDay?.let { topic ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(40.dp).background(colors.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(topicSymbol(topic.icon)), contentDescription = null, Modifier.size(22.dp), tint = colors.accent)
                }
                Text(topic.title, style = tileText(), color = colors.accent)
            }
        }
    }
}

@Composable
private fun Tile(
    label: String,
    @DrawableRes icon: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: OneUiArtwork? = null,
    fillHeight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    OneUiCard(
        modifier = if (fillHeight) modifier.fillMaxHeight() else modifier,
        onClick = onClick,
        artwork = artwork,
        contentPadding = PaddingValues(0.dp),
    ) {
        val onArtwork = LocalContentColor.current
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)
                .heightIn(min = TileMinHeight)
                .padding(TilePadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            TileLabel(
                label,
                icon,
                color = if (artwork != null) onArtwork.copy(alpha = 0.85f) else OneUi.colors.secondaryText,
                iconTint = if (artwork != null) onArtwork else OneUi.colors.accent,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun TileLabel(text: String, @DrawableRes icon: Int?, color: Color, iconTint: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Icon(painterResource(icon), contentDescription = null, Modifier.size(20.dp), tint = iconTint)
        Text(text, style = OneUi.typography.listSummary.copy(fontWeight = FontWeight.Medium), color = color)
    }
}
