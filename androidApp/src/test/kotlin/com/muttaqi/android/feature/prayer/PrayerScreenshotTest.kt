package com.muttaqi.android.feature.prayer

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaCompass
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaPhase
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaState
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.ZoneId

/**
 * The Qibla and the prayer times for Karachi, as the iOS screenshots were taken there, on an iPhone 17 Pro Max-sized
 * screen (440 × 956 points at 3×) to compare with them pixel for pixel
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PRO_MAX)
class PrayerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val karachi = Coordinates(24.8607, 67.0011)
    private val qibla = AdhanQiblaRepository().qiblaDirection(karachi)
    private val ready = QiblaState(phase = QiblaPhase.Ready(qibla), isCompassAvailable = true)

    private fun reading(heading: Double, accuracy: Double = 5.0) =
        QiblaCompass(CompassHeading(heading, accuracy), dialRotation = heading, turnAngle = qibla.bearing - heading)

    @Test
    fun withoutACompass() = compose.captureLightAndDark("qibla_no_compass") {
        QiblaScreen(ready.copy(isCompassAvailable = false), compass = null, onIntent = {}, onBack = {})
    }

    @Test
    fun turningToTheQibla() = compose.captureLightAndDark("qibla_turning") {
        QiblaScreen(ready, reading(200.0), onIntent = {}, onBack = {})
    }

    @Test
    fun facingTheQibla() = compose.captureLightAndDark("qibla_facing") {
        QiblaScreen(ready, reading(267.0), onIntent = {}, onBack = {})
    }

    @Test
    fun aCompassToCalibrate() = compose.captureLightAndDark("qibla_calibrate") {
        QiblaScreen(ready, reading(40.0, accuracy = -1.0), onIntent = {}, onBack = {})
    }

    @Test
    fun askingForLocation() = compose.captureLightAndDark("qibla_allow_location") {
        QiblaScreen(QiblaState(phase = QiblaPhase.NeedsLocation(LocationAccess.NotDetermined)), compass = null, onIntent = {}, onBack = {})
    }

    @Test
    fun locationRefused() = compose.captureLightAndDark("qibla_open_settings") {
        QiblaScreen(QiblaState(phase = QiblaPhase.NeedsLocation(LocationAccess.Denied)), compass = null, onIntent = {}, onBack = {})
    }

    @Test
    fun locating() = compose.captureLightAndDark("qibla_locating") {
        QiblaScreen(QiblaState(), compass = null, onIntent = {}, onBack = {})
    }

    @Test
    fun prayerTimes() {
        val zone = ZoneId.of("Asia/Karachi")
        val today = AdhanPrayerTimesRepository().prayerTimes(LocalDate(2026, 9, 30), karachi)!!
        compose.captureLightAndDark("prayer_times") {
            Box(Modifier.fillMaxSize()) {
                SoftBackdrop()
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(28.dp), horizontalAlignment = Alignment.End) {
                    NextPrayerPill(UpcomingPrayer(Prayer.Asr, today.asr), needsLocation = false, onSetLocation = {}, zone = zone)
                    NextPrayerPill(upcoming = null, needsLocation = true, onSetLocation = {}, zone = zone)
                    PrayerTimesStrip(today, next = Prayer.Asr, zone = zone)
                }
            }
        }
    }
}

/** An iPhone 17 Pro Max-sized screen, the simulator the iOS screenshots for prayer and onboarding were taken on */
const val PRO_MAX = "w440dp-h956dp-xxhdpi"
