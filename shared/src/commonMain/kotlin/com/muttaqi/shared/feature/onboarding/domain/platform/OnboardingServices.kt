package com.muttaqi.shared.feature.onboarding.domain.platform

interface NotificationPermission {
    fun request(onResult: (granted: Boolean) -> Unit)
}
