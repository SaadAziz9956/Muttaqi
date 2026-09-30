package com.muttaqi.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.component.brush
import com.muttaqi.android.designsystem.component.softClickable
import com.muttaqi.android.designsystem.component.softPressScale
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.home.presentation.DailyCard
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage

/** A wide floating row that opens a surah, e.g. where the reader left off */
@Composable
internal fun SurahShortcut(surahNumber: Int, title: String, subtitle: String, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftCard(Modifier.fillMaxWidth(), cornerRadius = 36.dp, onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(50.dp).clip(CircleShape).background(SoftArtwork.Forest.brush(soft.dark)).border(2.dp, soft.rim, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("$surahNumber", style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp), color = Color.White)
            }
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = soft.appPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            SoftPillSurface(Modifier.size(40.dp), fill = soft.brandGreen, rim = false) {
                Icon(painterResource(R.drawable.ic_arrow_right_01_linear), null, Modifier.size(18.dp), tint = Color.White)
            }
        }
    }
}

@Composable
internal fun AyahOfTheDayCard(dailyAyah: DailyAyah, onIntent: (HomeIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    DailyCardSurface(DailyCard.Ayah, onIntent, onClick = { onIntent(HomeIntent.AyahOfTheDayTapped) }) {
        CardTitle("Ayah of the Day")
        ArabicText(dailyAyah.ayah.arabicText, Modifier.padding(top = 22.dp), fontSize = 21.sp, lineSpacing = 10.sp)
        dailyAyah.ayah.translation?.let {
            TranslationText(it, Modifier.padding(top = 12.dp), fontSize = 14.sp, lineSpacing = 4.sp)
        }
        Text(
            "Quran (${dailyAyah.reference})",
            Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = soft.brandTeal,
        )
    }
}

/** A short authentic hadith from Explore, in full as HadeethEnc publishes it */
@Composable
internal fun HadithOfTheDayCard(hadith: HadithPassage, onIntent: (HomeIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    val urdu = hadith.translation.isArabicScript()
    DailyCardSurface(DailyCard.Hadith, onIntent) {
        CardTitle("Hadith of the Day")
        if (hadith.arabic.isNotEmpty()) {
            ArabicText(hadith.arabic, Modifier.padding(top = 22.dp), fontSize = 19.sp, lineSpacing = 9.sp)
        }
        TranslationText(hadith.translation, Modifier.padding(top = 12.dp), fontSize = 14.sp, lineSpacing = if (urdu) 8.sp else 4.sp)
        if (hadith.source.isArabicScript()) {
            TranslationText(hadith.source, Modifier.padding(top = 12.dp), fontSize = 11.sp, color = soft.brandTeal, lineSpacing = 0.sp)
        } else {
            Text(
                hadith.source,
                Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = soft.brandTeal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun DuaOfTheDayCard(dua: QuranicDua, onIntent: (HomeIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    val urdu = dua.translation.isArabicScript()
    DailyCardSurface(DailyCard.Dua, onIntent, horizontalAlignment = Alignment.Start) {
        CardTitle("Dua of the Day", Modifier.fillMaxWidth())
        ArabicText(
            dua.arabic,
            Modifier.fillMaxWidth().padding(top = 22.dp),
            fontSize = 20.sp,
            textAlign = TextAlign.Right,
            lineSpacing = 10.sp,
        )
        Text(
            dua.transliteration,
            Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
            color = soft.appPrimary,
        )
        TranslationText(
            dua.translation,
            Modifier.fillMaxWidth().padding(top = 12.dp),
            fontSize = 14.sp,
            textAlign = if (urdu) TextAlign.Right else TextAlign.Left,
            lineSpacing = 4.sp,
        )
        Text(
            "Quran (${dua.reference})",
            Modifier.fillMaxWidth().padding(top = 14.dp),
            style = MaterialTheme.typography.labelSmall,
            color = soft.brandTeal,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A daily card floating on Home like its tiles, with its share button in the corner; a long press offers Copy and
 * Share, as iOS's context menu does. [onClick] makes the whole card tappable, as the Ayah of the Day is
 */
@Composable
private fun DailyCardSurface(
    card: DailyCard,
    onIntent: (HomeIntent) -> Unit,
    onClick: (() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    val openMenu = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        showMenu = true
    }
    SoftCard(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.softPressScale(interaction) else Modifier)) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        // The soft tap on a press, as every soft card gives, and one haptic for the long press
                        Modifier.combinedClickable(
                            interactionSource = interaction,
                            indication = ripple(),
                            hapticFeedbackEnabled = false,
                            onLongClick = openMenu,
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onClick()
                        }
                    } else {
                        Modifier
                            .pointerInput(Unit) { detectTapGestures(onLongPress = { openMenu() }) }
                            .semantics { onLongClick(label = "Copy or share") { openMenu(); true } }
                    },
                )
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 22.dp),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
        ShareButton(Modifier.align(Alignment.TopEnd).padding(10.dp)) { onIntent(HomeIntent.ShareTapped(card)) }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Copy") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_copy_linear), null, Modifier.size(20.dp)) },
                onClick = {
                    showMenu = false
                    onIntent(HomeIntent.CopyTapped(card))
                },
            )
            DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_export_arrow_01_linear), null, Modifier.size(20.dp)) },
                onClick = {
                    showMenu = false
                    onIntent(HomeIntent.ShareTapped(card))
                },
            )
        }
    }
}

/** The share icon in a card's corner, with room around it to tap */
@Composable
private fun ShareButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .softPressScale(interaction)
            .clip(CircleShape)
            .softClickable(interaction, onClick)
            .padding(6.dp)
            .semantics { contentDescription = "Share" },
    ) {
        Icon(painterResource(R.drawable.ic_export_arrow_01_linear), null, Modifier.size(18.dp), tint = MuttaqiTheme.soft.textSecondary)
    }
}

@Composable
private fun CardTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier, style = MaterialTheme.typography.labelSmall, color = MuttaqiTheme.soft.appPrimary, textAlign = TextAlign.Center)
}
