package com.muttaqi.android.feature.onboarding

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiArtwork
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiButtonStyle
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiProgress
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.android.designsystem.oneui.OneUiTextField
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingEffect
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingIntent
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingState
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingVerses
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel

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

@Composable
fun OnboardingScreen(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    OneUiSurface {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout).union(WindowInsets.ime))) {
            AnimatedVisibility(state.step != OnboardingStep.Setup, enter = fadeIn(), exit = fadeOut()) {
                AppName(Modifier.fillMaxWidth().padding(top = 48.dp))
            }
            AnimatedContent(
                targetState = state.step,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    val duration = tween<IntOffset>(450, easing = OneUiDefaults.Easing)
                    (slideInHorizontally(duration) { it } + fadeIn(tween(450))) togetherWith
                        (slideOutHorizontally(duration) { -it / 3 } + fadeOut(tween(450)))
                },
                label = "step",
            ) { step ->
                Column(Modifier.fillMaxSize().padding(horizontal = OneUiDefaults.ScreenMargin), horizontalAlignment = Alignment.CenterHorizontally) {
                    when (step) {
                        OnboardingStep.Welcome -> WelcomeStep { onIntent(OnboardingIntent.Begin) }
                        OnboardingStep.Name -> NameStep(state.name, { onIntent(OnboardingIntent.NameChanged(it)) }) { onIntent(OnboardingIntent.SaveName) }
                        OnboardingStep.Goals -> GoalsStep { onIntent(OnboardingIntent.Next) }
                        OnboardingStep.Notification -> PermissionStep(
                            icon = R.drawable.ic_notifications,
                            title = "Enable Notification",
                            detail = "Enable Notification so you don't miss daily Quran ayah and Azkar and Namaz Alarms.",
                            question = "Would you like to turn on Notifications?",
                            action = "Turn on",
                            onAction = { onIntent(OnboardingIntent.RequestNotification) },
                            onSkip = { onIntent(OnboardingIntent.SkipNotification) },
                        )
                        OnboardingStep.Location -> PermissionStep(
                            icon = R.drawable.ic_location_on,
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
}

@Composable
private fun AppName(modifier: Modifier = Modifier) {
    ArabicText(
        "متقي",
        modifier,
        style = OneUi.typography.largeTitle.copy(fontSize = 57.sp),
        color = OneUi.colors.accent,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun stepTitle(): TextStyle = OneUi.typography.sectionTitle.copy(fontSize = 24.sp, lineHeight = 32.sp)

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OneUiButton(text, onClick, modifier.padding(horizontal = 12.dp).fillMaxWidth().heightIn(min = 52.dp))
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OneUiButton(text, onClick, modifier.padding(horizontal = 12.dp).fillMaxWidth().heightIn(min = 52.dp), style = OneUiButtonStyle.Text)
}

@Composable
private fun ColumnScope.WelcomeStep(onBegin: () -> Unit) {
    val verse = OnboardingVerses.basmala
    Spacer(Modifier.weight(1f))
    OneUiCard(Modifier.fillMaxWidth(), artwork = OneUiArtwork.Forest, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 40.dp)) {
        val content = LocalContentColor.current
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            QuranText(verse.arabic, Modifier.fillMaxWidth(), fontSize = 32.sp, color = content)
            TranslationText(verse.translation, Modifier.fillMaxWidth(), style = OneUi.typography.body, color = content.copy(alpha = 0.9f), textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton("Begin", onBegin, Modifier.padding(bottom = 24.dp))
}

@Composable
private fun ColumnScope.NameStep(name: String, onNameChange: (String) -> Unit, onSave: () -> Unit) {
    Spacer(Modifier.weight(1f))
    Text("What should we call you?", Modifier.padding(horizontal = 12.dp), style = stepTitle(), color = OneUi.colors.text, textAlign = TextAlign.Center)
    OneUiTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = "Type here...",
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
    )
    Spacer(Modifier.weight(1f))
    PrimaryButton("Save", onSave, Modifier.padding(bottom = 24.dp))
}

@Composable
private fun ColumnScope.GoalsStep(onBegin: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val verse = OnboardingVerses.lovesThePure
    Spacer(Modifier.weight(1f))
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
        Text("We will help you to achieve your Muslim Goals", style = type.sectionTitle, color = colors.text)
        Text(
            "by using our App on the daily basis you will",
            Modifier.padding(top = 4.dp),
            style = type.listSummary,
            color = colors.secondaryText,
        )
        Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            GoalRow(R.drawable.ic_volunteer_activism, "Become a better Muslim")
            GoalRow(R.drawable.ic_menu_book, "Read Quran with translation")
            GoalRow(R.drawable.ic_self_improvement, "Zikr o Azkar")
            GoalRow(R.drawable.ic_lightbulb, "Learn Sunnah")
        }
    }
    Text(
        "In Shaa Allah",
        Modifier.padding(top = 20.dp),
        style = type.listTitle.copy(fontWeight = FontWeight.Medium),
        color = colors.accent,
    )
    Spacer(Modifier.weight(1f))
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuranText(verse.arabic, Modifier.fillMaxWidth(), fontSize = 24.sp, color = colors.text)
        TranslationText(
            verse.translation,
            Modifier.fillMaxWidth(),
            style = type.listSummary,
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        verse.source?.let {
            Text(it, style = type.caption, color = colors.secondaryText)
        }
    }
    PrimaryButton("Begin", onBegin, Modifier.padding(top = 24.dp, bottom = 24.dp))
}

@Composable
private fun GoalRow(@DrawableRes icon: Int, text: String) {
    val colors = OneUi.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(40.dp).background(colors.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, Modifier.size(22.dp), tint = colors.accent)
        }
        Text(text, style = OneUi.typography.listTitle, color = colors.text)
    }
}

@Composable
private fun ColumnScope.PermissionStep(
    @DrawableRes icon: Int,
    title: String,
    detail: String,
    question: String,
    action: String,
    onAction: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Spacer(Modifier.weight(1f))
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).background(colors.accent, CircleShape), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), contentDescription = null, Modifier.size(32.dp), tint = colors.onAccent)
            }
            Text(title, Modifier.padding(top = 24.dp), style = stepTitle(), color = colors.text, textAlign = TextAlign.Center)
            Text(
                detail,
                Modifier.padding(top = 8.dp),
                style = type.listSummary,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.weight(1f))
    Text(question, Modifier.padding(horizontal = 12.dp), style = type.listTitle.copy(fontWeight = FontWeight.Medium), color = colors.text, textAlign = TextAlign.Center)
    PrimaryButton(action, onAction, Modifier.padding(top = 16.dp))
    SecondaryButton("Not now", onSkip, Modifier.padding(top = 8.dp, bottom = 24.dp))
}

@Composable
private fun ColumnScope.SetupStep(error: String?, onRetry: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Spacer(Modifier.weight(1f))
    AppName()
    OneUiCard(Modifier.padding(top = 24.dp).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (error == null) {
                Text("Setting up for first time", style = type.sectionTitle, color = colors.text, textAlign = TextAlign.Center)
                Text(
                    "Downloading Quran data...",
                    Modifier.padding(top = 8.dp),
                    style = type.listSummary,
                    color = colors.secondaryText,
                )
                OneUiProgress(Modifier.padding(top = 20.dp))
            } else {
                Text("Setup Failed", style = type.sectionTitle, color = colors.text, textAlign = TextAlign.Center)
                Text(
                    error,
                    Modifier.padding(top = 8.dp),
                    style = type.listSummary,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    if (error != null) {
        PrimaryButton("Retry", onRetry, Modifier.padding(top = 24.dp))
    }
    Spacer(Modifier.weight(1f))
}
