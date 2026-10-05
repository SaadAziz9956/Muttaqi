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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
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
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(state.step != OnboardingStep.Setup, enter = fadeIn(), exit = fadeOut()) {
                AppName(Modifier.fillMaxWidth().padding(top = 48.dp))
            }
            AnimatedContent(
                targetState = state.step,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    val duration = tween<IntOffset>(450)
                    (slideInHorizontally(duration) { it } + fadeIn(tween(450))) togetherWith
                        (slideOutHorizontally(duration) { -it / 3 } + fadeOut(tween(450)))
                },
                label = "step",
            ) { step ->
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = ButtonDefaults.MediumContainerHeight
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(size),
        contentPadding = ButtonDefaults.contentPaddingFor(size),
    ) {
        Text(text, style = ButtonDefaults.textStyleFor(size))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = ButtonDefaults.MediumContainerHeight
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(size),
        contentPadding = ButtonDefaults.contentPaddingFor(size),
    ) {
        Text(text, style = ButtonDefaults.textStyleFor(size))
    }
}

@Composable
private fun ColumnScope.WelcomeStep(onBegin: () -> Unit) {
    val verse = OnboardingVerses.basmala
    Spacer(Modifier.weight(1f))
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QuranText(verse.arabic, Modifier.fillMaxWidth(), fontSize = MaterialTheme.typography.headlineLarge.fontSize)
            TranslationText(verse.translation, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton("Begin", onBegin, Modifier.padding(bottom = 24.dp))
}

@Composable
private fun ColumnScope.NameStep(name: String, onNameChange: (String) -> Unit, onSave: () -> Unit) {
    Spacer(Modifier.weight(1f))
    Text("What should we call you?", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    TextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        placeholder = { Text("Type here...") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
    )
    Spacer(Modifier.weight(1f))
    PrimaryButton("Save", onSave, Modifier.padding(bottom = 24.dp))
}

@Composable
private fun ColumnScope.GoalsStep(onBegin: () -> Unit) {
    val verse = OnboardingVerses.lovesThePure
    Spacer(Modifier.weight(1f))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("We will help you to achieve your Muslim Goals", style = MaterialTheme.typography.titleLarge)
            Text(
                "by using our App on the daily basis you will",
                Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GoalRow(R.drawable.ic_volunteer_activism, "Become a better Muslim")
                GoalRow(R.drawable.ic_menu_book, "Read Quran with translation")
                GoalRow(R.drawable.ic_self_improvement, "Zikr o Azkar")
                GoalRow(R.drawable.ic_lightbulb, "Learn Sunnah")
            }
        }
    }
    Text(
        "In Shaa Allah",
        Modifier.padding(top = 20.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.weight(1f))
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuranText(verse.arabic, Modifier.fillMaxWidth(), fontSize = MaterialTheme.typography.headlineSmall.fontSize)
        TranslationText(
            verse.translation,
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        verse.source?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    PrimaryButton("Begin", onBegin, Modifier.padding(top = 24.dp, bottom = 24.dp))
}

@Composable
private fun GoalRow(@DrawableRes icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge)
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
    Spacer(Modifier.weight(1f))
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, MaterialShapes.Cookie9Sided.toShape()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(title, Modifier.padding(top = 24.dp), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                detail,
                Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.weight(1f))
    Text(question, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    PrimaryButton(action, onAction, Modifier.padding(top = 16.dp))
    SecondaryButton("Not now", onSkip, Modifier.padding(top = 8.dp, bottom = 24.dp))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.SetupStep(error: String?, onRetry: () -> Unit) {
    Spacer(Modifier.weight(1f))
    AppName()
    Card(Modifier.padding(top = 24.dp).fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (error == null) {
                Text("Setting up for first time", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    "Downloading Quran data...",
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LoadingIndicator(Modifier.padding(top = 16.dp))
            } else {
                Text("Setup Failed", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    error,
                    Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
