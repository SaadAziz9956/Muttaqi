package com.muttaqi.shared.feature.onboarding.domain.platform

// What onboarding needs from each app. They report through callbacks rather than suspend functions, so Swift classes
// can implement them; the use cases turn them into coroutines.

/** The system's permission to show notifications: UserNotifications on iOS, POST_NOTIFICATIONS on Android */
interface NotificationPermission {
    /** Shows the system's request while it's undecided, then reports whether notifications are allowed, once */
    fun request(onResult: (granted: Boolean) -> Unit)
}

/**
 * The download the first launch waits for: the Quran's Arabic, transliteration and English translation. The Quran
 * feature does it, and each app gives onboarding its own until that feature is shared
 */
interface FirstLaunchSetup {
    /** Reports [onDone] or, with a message to show, [onFailed], once */
    fun run(onDone: () -> Unit, onFailed: (message: String) -> Unit)
}
