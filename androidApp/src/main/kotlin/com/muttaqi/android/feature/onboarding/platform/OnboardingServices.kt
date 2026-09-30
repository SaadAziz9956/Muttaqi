package com.muttaqi.android.feature.onboarding.platform

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.muttaqi.android.platform.ActivityPermissions
import com.muttaqi.shared.feature.onboarding.domain.platform.FirstLaunchSetup
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Notification permission, which Android 13 and later ask for; before that notifications are on unless turned off */
class AndroidNotificationPermission(
    private val context: Context,
    private val permissions: ActivityPermissions,
) : NotificationPermission {
    override fun request(onResult: (granted: Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return onResult(enabled())
        permissions.request(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { onResult(enabled()) }
    }

    private fun enabled() = NotificationManagerCompat.from(context).areNotificationsEnabled()
}

/**
 * The first-launch download. The Android app's Quran isn't built yet, so there's nothing to fetch; once the Quran is
 * shared, onboarding runs its download on both platforms
 */
object NothingToDownload : FirstLaunchSetup {
    override fun run(onDone: () -> Unit, onFailed: (message: String) -> Unit) = onDone()
}

/** Android's notification permission and first-launch setup for the shared onboarding */
val onboardingServicesModule = module {
    single<NotificationPermission> { AndroidNotificationPermission(androidContext(), get()) }
    single<FirstLaunchSetup> { NothingToDownload }
}
