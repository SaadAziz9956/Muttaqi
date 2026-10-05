package com.muttaqi.android.feature.names

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.names.domain.model.AllahName

@Composable
fun NameCard(name: AllahName, modifier: Modifier = Modifier, minHeight: Dp = 300.dp) {
    Card(
        modifier.clearAndSetSemantics { contentDescription = "${name.number}, ${name.arabic}, ${name.transliteration}, ${name.meaning}" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = minHeight)) {
            NameNumber(
                name.number,
                Modifier.align(Alignment.TopStart).padding(16.dp),
                container = MaterialTheme.colorScheme.primary,
            )
            Column(
                Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 24.dp, vertical = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ArabicText(name.arabic, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
                Text(
                    name.transliteration,
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                TranslationText(
                    name.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineSpacing = if (name.meaning.isArabicScript()) 6.sp else 2.sp,
                )
            }
        }
    }
}

@Composable
internal fun NameNumber(number: Int, modifier: Modifier = Modifier, container: Color = MaterialTheme.colorScheme.secondaryContainer) {
    Surface(modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge, color = container) {
        Box(contentAlignment = Alignment.Center) {
            Text("$number", style = MaterialTheme.typography.labelLarge)
        }
    }
}
