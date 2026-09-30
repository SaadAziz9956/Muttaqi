package com.muttaqi.android.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingEffect
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingIntent
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingState
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingVerse
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingVerses
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel

/** The first launch; [onFinished] once it's done and saved */
@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.Finished -> onFinished()
            }
        }
    }
    OnboardingScreen(state, viewModel::dispatch)
}

/**
 * One step at a time under the app's name, which stays still while the steps move on like pages; setup centres its
 * own. On the plain system background, as on iOS, rather than the soft backdrop
 */
@Composable
fun OnboardingScreen(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    val colors = OnboardingColors.current()
    Column(Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        AnimatedVisibility(state.step != OnboardingStep.Setup, enter = fadeIn(), exit = fadeOut()) {
            AppName(Modifier.fillMaxWidth().padding(top = 48.dp))
        }
        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            // Pushed in from the trailing edge, as iOS moves between the steps
            transitionSpec = {
                val duration = tween<androidx.compose.ui.unit.IntOffset>(450)
                (slideInHorizontally(duration) { it } + fadeIn(tween(450))) togetherWith
                    (slideOutHorizontally(duration) { -it / 3 } + fadeOut(tween(450)))
            },
            label = "step",
        ) { step ->
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                when (step) {
                    OnboardingStep.Welcome -> WelcomeStep { onIntent(OnboardingIntent.Begin) }
                    OnboardingStep.Name -> NameStep(state.name, { onIntent(OnboardingIntent.NameChanged(it)) }) { onIntent(OnboardingIntent.SaveName) }
                    OnboardingStep.Goals -> GoalsStep { onIntent(OnboardingIntent.Next) }
                    OnboardingStep.Notification -> PermissionStep(
                        title = "Enable Notification",
                        detail = "Enable Notification so you don't miss\ndaily Quran ayah and Azkar and Namaz Alarms.",
                        question = "Would you like to turn on Notifications?",
                        action = "Turn on",
                        onAction = { onIntent(OnboardingIntent.RequestNotification) },
                        onSkip = { onIntent(OnboardingIntent.SkipNotification) },
                    )
                    OnboardingStep.Location -> PermissionStep(
                        title = "Select Location",
                        detail = "Select your current location to get latest Namaz timing",
                        question = "Find your City",
                        action = "Find",
                        onAction = { onIntent(OnboardingIntent.RequestLocation) },
                        onSkip = { onIntent(OnboardingIntent.SkipLocation) },
                    )
                    OnboardingStep.Setup -> SetupStep(state.setupError) { onIntent(OnboardingIntent.RetrySetup) }
                }
            }
        }
    }
}

@Composable
private fun AppName(modifier: Modifier = Modifier) {
    Text(
        "متقي",
        modifier,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 60.sp, textDirection = TextDirection.Rtl),
        color = MuttaqiTheme.soft.appPrimary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ColumnScope.WelcomeStep(onBegin: () -> Unit) {
    val colors = OnboardingColors.current()
    val verse = OnboardingVerses.basmala
    Spacer(Modifier.weight(1f))
    Column(Modifier.offset(y = (-60).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VerseArabic(verse, 32.sp, colors.textPrimary)
        Text(verse.translation, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.weight(1f))
    OnboardingButton("Begin", onBegin, Modifier.padding(bottom = 35.dp))
}

@Composable
private fun ColumnScope.NameStep(name: String, onNameChange: (String) -> Unit, onSave: () -> Unit) {
    val colors = OnboardingColors.current()
    val soft = MuttaqiTheme.soft
    Spacer(Modifier.weight(1f))
    Column(Modifier.offset(y = (-60).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("What should we call you?", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(47.dp)
                .background(colors.textField, RoundedCornerShape(12.dp)),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(soft.appPrimary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            decorationBox = { field ->
                Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    if (name.isEmpty()) {
                        Text("Type here...", style = MaterialTheme.typography.bodyMedium, color = soft.textSecondary)
                    }
                    field()
                }
            },
        )
    }
    Spacer(Modifier.weight(1f))
    OnboardingButton("Save", onSave, Modifier.padding(bottom = 35.dp))
}

@Composable
private fun ColumnScope.GoalsStep(onBegin: () -> Unit) {
    val colors = OnboardingColors.current()
    val soft = MuttaqiTheme.soft
    val verse = OnboardingVerses.lovesThePure
    Spacer(Modifier.weight(1f))
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("We will help you to achieve your Muslim Goals", style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text(
            "by using our App on the daily basis you will",
            Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = soft.textSecondary,
        )
        Column(Modifier.padding(top = 26.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Become a better Muslim", "Read Quran with translation", "Zikr o Azkar", "Learn Sunnah").forEach { goal ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(4.dp).background(colors.textMediumGrey, CircleShape))
                    Text(goal, style = MaterialTheme.typography.labelSmall, color = colors.textMediumGrey)
                }
            }
        }
        Text(
            "In Shaa Allah",
            Modifier.fillMaxWidth().padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = soft.appPrimary,
            textAlign = TextAlign.Center,
        )
    }
    OnboardingButton("Begin", onBegin, Modifier.padding(top = 58.dp))
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(bottom = 35.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        VerseArabic(verse, 26.sp, colors.textPrimary)
        Text(verse.translation, style = MaterialTheme.typography.bodySmall, color = soft.textSecondary)
        verse.source?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = soft.textSecondary) }
    }
}

/** A step asking for a permission, as notifications and location do */
@Composable
private fun ColumnScope.PermissionStep(
    title: String,
    detail: String,
    question: String,
    action: String,
    onAction: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = OnboardingColors.current()
    val soft = MuttaqiTheme.soft
    Spacer(Modifier.weight(1f))
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text(detail, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(bottom = 35.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(question, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        OnboardingButton(
            action,
            onAction,
            Modifier.padding(top = 17.dp),
            width = 100.dp,
            height = 33.dp,
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.labelSmall,
        )
        TextButton(onClick = onSkip, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(24.dp)) {
            Text(
                "Not now",
                style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.Underline),
                color = soft.textSecondary,
            )
        }
    }
    Spacer(Modifier.weight(1f))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.SetupStep(error: String?, onRetry: () -> Unit) {
    val colors = OnboardingColors.current()
    val soft = MuttaqiTheme.soft
    Spacer(Modifier.weight(1f))
    AppName()
    if (error == null) {
        Text("Setting up for first time", Modifier.padding(top = 22.dp), style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text("Downloading Quran data...", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = soft.textSecondary)
        LoadingIndicator(Modifier.padding(top = 12.dp).size(36.dp), color = soft.appPrimary)
    } else {
        Text("Setup Failed", Modifier.padding(top = 22.dp), style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Text(
            error,
            Modifier.padding(horizontal = 32.dp).padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = soft.textSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 24.dp).size(160.dp, 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = soft.appPrimary, contentColor = colors.onPrimary),
        ) {
            Text("Retry", style = MaterialTheme.typography.titleSmall)
        }
    }
    Spacer(Modifier.weight(1f))
}

/** A verse in the Quran font as iOS sets it on these steps: the published text as it is, not re-encoded for the font */
@Composable
private fun VerseArabic(verse: OnboardingVerse, size: TextUnit, color: Color) {
    Text(verse.arabic, style = TextStyle(fontFamily = QuranFont, fontSize = size, textDirection = TextDirection.Rtl), color = color, textAlign = TextAlign.Center)
}

/** The onboarding's own teal button */
@Composable
private fun OnboardingButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    height: Dp = 47.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    val colors = OnboardingColors.current()
    Button(
        onClick = onClick,
        modifier = modifier.size(width, height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryButton, contentColor = Color.White),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(label, style = textStyle)
    }
}

/** The iOS asset catalogue's onboarding colours, which aren't part of the soft style */
private data class OnboardingColors(
    val background: Color,
    val textPrimary: Color,
    val onSurfaceVariant: Color,
    val textMediumGrey: Color,
    val textField: Color,
    val primaryButton: Color,
    val onPrimary: Color,
) {
    companion object {
        private val Light = OnboardingColors(
            background = Color.White,
            textPrimary = Color(0xFF393939),
            onSurfaceVariant = Color(0xFFC3CDCE),
            textMediumGrey = Color(0xFF5F5F5F),
            textField = Color(0xFFEAEAEA),
            primaryButton = Color(0xFF81CACF),
            onPrimary = Color.White,
        )
        private val Dark = OnboardingColors(
            background = Color.Black,
            textPrimary = Color(0xFFEBEBEB),
            onSurfaceVariant = Color(0xFF8C9A9C),
            textMediumGrey = Color(0xFFAEAEB2),
            textField = Color(0xFF2C2C2E),
            primaryButton = Color(0xFF2E6A6F),
            onPrimary = Color(0xFF0B2219),
        )

        @Composable
        fun current() = if (MuttaqiTheme.soft.dark) Dark else Light
    }
}
