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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftButton
import com.muttaqi.android.designsystem.component.SoftButtonKind
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.SoftTextField
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingEffect
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingIntent
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingState
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingVerse
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
    Box(Modifier.fillMaxSize()) {
        SoftBackdrop()
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            AnimatedVisibility(state.step != OnboardingStep.Setup, enter = fadeIn(), exit = fadeOut()) {
                AppName(Modifier.fillMaxWidth().padding(top = 48.dp))
            }
            AnimatedContent(
                targetState = state.step,
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            icon = R.drawable.ic_clock_linear,
                            title = "Enable Notification",
                            detail = "Enable Notification so you don't miss daily Quran ayah and Azkar and Namaz Alarms.",
                            question = "Would you like to turn on Notifications?",
                            action = "Turn on",
                            onAction = { onIntent(OnboardingIntent.RequestNotification) },
                            onSkip = { onIntent(OnboardingIntent.SkipNotification) },
                        )
                        OnboardingStep.Location -> PermissionStep(
                            icon = R.drawable.ic_home_qibla,
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
    Text(
        "متقي",
        modifier,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 60.sp, textDirection = TextDirection.Rtl),
        color = MuttaqiTheme.soft.appPrimary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StepTitle(text: String, modifier: Modifier = Modifier, size: TextUnit = 22.sp, textAlign: TextAlign = TextAlign.Center) {
    Text(text, modifier, style = MaterialTheme.typography.titleMedium.copy(fontSize = size, fontWeight = FontWeight.Medium), color = MuttaqiTheme.soft.appPrimary, textAlign = textAlign)
}

@Composable
private fun StepDetail(text: String, modifier: Modifier = Modifier, color: Color = MuttaqiTheme.soft.textSecondary, textAlign: TextAlign = TextAlign.Center) {
    Text(text, modifier, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 26.sp), color = color, textAlign = textAlign)
}

@Composable
private fun ColumnScope.WelcomeStep(onBegin: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val verse = OnboardingVerses.basmala
    Spacer(Modifier.weight(1f))
    SoftCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), artwork = SoftArtwork.Dawn) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VerseArabic(verse, 32.sp)
            StepDetail(verse.translation, color = soft.textPrimary)
        }
    }
    Spacer(Modifier.weight(1f))
    SoftButton("Begin", onBegin, Modifier.padding(bottom = 40.dp))
}

@Composable
private fun ColumnScope.NameStep(name: String, onNameChange: (String) -> Unit, onSave: () -> Unit) {
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        StepTitle("What should we call you?")
        SoftTextField(name, onNameChange, "Type here...", onDone = onSave)
    }
    Spacer(Modifier.weight(1f))
    SoftButton("Save", onSave, Modifier.padding(bottom = 40.dp))
}

@Composable
private fun ColumnScope.GoalsStep(onBegin: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val verse = OnboardingVerses.lovesThePure
    Spacer(Modifier.weight(1f))
    SoftCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            StepTitle("We will help you to achieve your Muslim Goals", size = 20.sp, textAlign = TextAlign.Start)
            StepDetail("by using our App on the daily basis you will", Modifier.padding(top = 6.dp), textAlign = TextAlign.Start)
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalRow(R.drawable.ic_lovely_linear, "Become a better Muslim")
                GoalRow(R.drawable.ic_book_open_linear, "Read Quran with translation")
                GoalRow(R.drawable.ic_repeat_circle_linear, "Zikr o Azkar")
                GoalRow(R.drawable.ic_lamp_on_linear, "Learn Sunnah")
            }
        }
    }
    StepTitle("In Shaa Allah", Modifier.padding(top = 20.dp), size = 16.sp)
    SoftButton("Begin", onBegin, Modifier.padding(top = 22.dp))
    Spacer(Modifier.weight(1f))
    Column(
        Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        VerseArabic(verse, 26.sp)
        Text(verse.translation, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = soft.textPrimary, textAlign = TextAlign.Center)
        verse.source?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary) }
    }
}

@Composable
private fun GoalRow(@DrawableRes icon: Int, text: String) {
    val soft = MuttaqiTheme.soft
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).background(soft.tintedSurface, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, Modifier.size(18.dp), tint = soft.brandTeal)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp), color = soft.textPrimary)
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
    val soft = MuttaqiTheme.soft
    Spacer(Modifier.weight(1f))
    SoftCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SoftPillSurface(Modifier.size(72.dp), fill = soft.brandGreen, rim = false) {
                Icon(painterResource(icon), null, Modifier.size(30.dp), tint = Color.White)
            }
            StepTitle(title, Modifier.padding(top = 22.dp))
            StepDetail(detail, Modifier.padding(top = 10.dp))
        }
    }
    Spacer(Modifier.weight(1f))
    Column(
        Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(question, style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), color = soft.textPrimary, textAlign = TextAlign.Center)
        SoftButton(action, onAction, Modifier.padding(top = 4.dp))
        SoftButton("Not now", onSkip, kind = SoftButtonKind.Secondary)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.SetupStep(error: String?, onRetry: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Spacer(Modifier.weight(1f))
    AppName()
    SoftCard(Modifier.padding(horizontal = 20.dp).padding(top = 28.dp).fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (error == null) {
                StepTitle("Setting up for first time", size = 20.sp)
                StepDetail("Downloading Quran data...", Modifier.padding(top = 8.dp))
                LoadingIndicator(Modifier.padding(top = 14.dp).size(40.dp), color = soft.appPrimary)
            } else {
                StepTitle("Setup Failed", size = 20.sp)
                StepDetail(error, Modifier.padding(top = 8.dp))
            }
        }
    }
    if (error != null) {
        SoftButton("Retry", onRetry, Modifier.padding(top = 28.dp))
    }
    Spacer(Modifier.weight(1f))
}

@Composable
private fun VerseArabic(verse: OnboardingVerse, size: TextUnit) {
    Text(
        verse.arabic,
        style = TextStyle(fontFamily = QuranFont, fontSize = size, textDirection = TextDirection.Rtl),
        color = MuttaqiTheme.soft.textPrimary,
        textAlign = TextAlign.Center,
    )
}
