package com.muttaqi.android.feature.onboarding.platform

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.muttaqi.android.platform.ActivityPermissions
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

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

val onboardingServicesModule = module {
    single<NotificationPermission> { AndroidNotificationPermission(androidContext(), get()) }
}
