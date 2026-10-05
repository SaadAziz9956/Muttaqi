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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.topicSymbol
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeState
import java.text.NumberFormat
import kotlin.math.roundToInt

private val Spacing = 12.dp
private val TileMinHeight = 128.dp

@Composable
internal fun HomeTiles(state: HomeState, qiblaArrow: () -> Double?, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing)) {
            QiblaTile(state, qiblaArrow, Modifier.weight(1f)) { onIntent(HomeIntent.QiblaTapped) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing)) {
                DhikrTile(state.dhikrToday, Modifier.fillMaxWidth()) { onIntent(HomeIntent.DhikrTapped) }
                NameTile(state, Modifier.fillMaxWidth()) { onIntent(HomeIntent.NameTapped) }
            }
        }
        JournalTile(state, onIntent)
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing)) {
            EmotionsTile(Modifier.weight(1f)) { onIntent(HomeIntent.EmotionsTapped) }
            TopicTile(state, Modifier.weight(1f)) { onIntent(HomeIntent.TopicTapped) }
        }
    }
}

@Composable
private fun QiblaTile(state: HomeState, qiblaArrow: () -> Double?, modifier: Modifier, onClick: () -> Unit) {
    val qibla = state.qibla
    val bearing = qibla?.bearing?.roundToInt()
    val colors = MaterialTheme.colorScheme
    Tile(
        label = "Qibla",
        icon = R.drawable.ic_explore,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
        labelColor = colors.onPrimaryContainer,
        fillHeight = true,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = if (bearing != null) "Qibla, $bearing degrees" else "Qibla"
            role = Role.Button
            onClick { onClick(); true }
        },
    ) {
        QiblaPointer(qiblaArrow, qibla?.bearing, Modifier.padding(vertical = 16.dp).size(112.dp).align(Alignment.CenterHorizontally))
        Column {
            if (qibla != null) {
                Text("$bearing°", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Makkah · ${NumberFormat.getIntegerInstance().format(qibla.distanceInKilometers)} km",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text("Find the Qibla", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun QiblaPointer(qiblaArrow: () -> Double?, bearing: Double?, modifier: Modifier = Modifier) {
    val arrow = qiblaArrow()
    val target = (arrow ?: bearing ?: 0.0).toFloat()
    val rotation by animateFloatAsState(target, tween(250), label = "qiblaArrow")
    Box(modifier.background(MaterialTheme.colorScheme.primary, MaterialShapes.Cookie9Sided.toShape()), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(R.drawable.ic_navigation_filled),
            contentDescription = null,
            Modifier.size(48.dp).graphicsLayer { rotationZ = if (arrow != null) rotation else target },
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun DhikrTile(said: Int, modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Dikr", icon = R.drawable.ic_self_improvement, onClick = onClick, modifier = modifier) {
        if (said > 0) {
            Column {
                Text(
                    NumberFormat.getIntegerInstance().format(said),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("said today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text("Begin today's dhikr", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NameTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Name of the day", icon = null, onClick = onClick, modifier = modifier) {
        state.nameOfTheDay?.let { name ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ArabicText(
                    name.arabic,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(name.transliteration, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun JournalTile(state: HomeState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    val entry = state.journalToday
    Card(
        onClick = { onIntent(HomeIntent.JournalTapped) },
        modifier = modifier.fillMaxWidth().semantics { contentDescription = "Journal" },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TileLabel("Journal", R.drawable.ic_edit_note, MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    entry?.preview ?: "What are you grateful for today?",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FilledIconButton(onClick = { onIntent(HomeIntent.TodaysEntryTapped) }) {
                Icon(
                    painterResource(if (entry == null) R.drawable.ic_add else R.drawable.ic_arrow_forward),
                    contentDescription = if (entry == null) "New entry" else "Today's entry",
                )
            }
        }
    }
}

@Composable
private fun EmotionsTile(modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Emotions", icon = R.drawable.ic_mood, onClick = onClick, modifier = modifier, fillHeight = true) {
        Text("How do you feel?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TopicTile(state: HomeState, modifier: Modifier, onClick: () -> Unit) {
    Tile(label = "Topic of the day", icon = null, onClick = onClick, modifier = modifier, fillHeight = true) {
        state.topicOfTheDay?.let { topic ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, MaterialShapes.Cookie6Sided.toShape()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(topicSymbol(topic.icon)),
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Text(topic.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
    colors: CardColors = CardDefaults.cardColors(),
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fillHeight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(onClick = onClick, modifier = if (fillHeight) modifier.fillMaxHeight() else modifier, colors = colors) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)
                .heightIn(min = TileMinHeight)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            TileLabel(label, icon, labelColor, Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
private fun TileLabel(text: String, @DrawableRes icon: Int?, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Icon(painterResource(icon), contentDescription = null, Modifier.size(20.dp), tint = color)
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
