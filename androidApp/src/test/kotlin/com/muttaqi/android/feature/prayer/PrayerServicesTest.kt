package com.muttaqi.android.feature.prayer

import android.Manifest
import android.app.Application
import com.muttaqi.android.feature.onboarding.platform.onboardingServicesModule
import com.muttaqi.android.feature.prayer.platform.prayerServicesModule
import com.muttaqi.android.platform.permissionsModule
import com.muttaqi.shared.di.androidPlatformModule
import com.muttaqi.shared.di.initKoin
import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The shared prayer and onboarding code wired to Android's own services, started as the app starts them */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class PrayerServicesTest {
    private val application: Application = RuntimeEnvironment.getApplication()

    @Before
    fun startKoin() {
        initKoin(androidPlatformModule(application)) {
            androidContext(application)
            modules(permissionsModule, prayerServicesModule, onboardingServicesModule)
        }
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun everyScreenAndServiceResolves() {
        assertNotNull(get<QiblaViewModel>(QiblaViewModel::class.java))
        assertNotNull(get<OnboardingViewModel>(OnboardingViewModel::class.java))
        assertFalse(get<IsOnboardingComplete>(IsOnboardingComplete::class.java)())
        // Robolectric's phone has no rotation-vector sensor, so there's no compass to follow
        assertFalse(get<HeadingProvider>(HeadingProvider::class.java).isAvailable)
    }

    @Test
    fun locationAccessFollowsThePermission() {
        val location = get<LocationProvider>(LocationProvider::class.java)
        assertEquals(LocationAccess.NotDetermined, location.access)
        // Approximate location is enough
        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(LocationAccess.Granted, location.access)
    }
}
