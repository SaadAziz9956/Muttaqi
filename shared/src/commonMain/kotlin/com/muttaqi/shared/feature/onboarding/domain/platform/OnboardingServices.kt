package com.muttaqi.shared.feature.onboarding.domain.platform

/**
 * The system's permission to show notifications, implemented by each app: UserNotifications on iOS,
 * POST_NOTIFICATIONS on Android. It reports through a callback rather than a suspend function, so a Swift class can
 * implement it; the use case turns it into a coroutine
 */
interface NotificationPermission {
    /** Shows the system's request while it's undecided, then reports whether notifications are allowed, once */
    fun request(onResult: (granted: Boolean) -> Unit)
}
