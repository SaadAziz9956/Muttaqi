package com.muttaqi.shared.feature.onboarding.domain.usecase

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.feature.onboarding.domain.platform.FirstLaunchSetup
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Whether the first launch is done, so the app opens straight to Home */
class IsOnboardingComplete(private val repository: OnboardingRepository) {
    operator fun invoke(): Boolean = repository.isComplete()
}

/** Keeps the name the reader gave, as they typed it; a blank name isn't saved */
class SaveUserName(private val repository: OnboardingRepository) {
    operator fun invoke(name: String): Boolean {
        if (name.isBlank()) return false
        repository.saveUserName(name)
        return true
    }
}

/** Whether the reader allowed notifications, asking them first if they haven't decided */
class RequestNotificationPermission(private val permission: NotificationPermission) {
    suspend operator fun invoke(): Boolean = suspendCancellableCoroutine { continuation ->
        permission.request { granted -> if (continuation.isActive) continuation.resume(granted) }
    }
}

/** Runs the first-launch download and, once it's done, marks onboarding complete */
class FinishOnboarding(private val setup: FirstLaunchSetup, private val repository: OnboardingRepository) {
    suspend operator fun invoke(): Outcome<Unit> {
        val outcome = suspendCancellableCoroutine<Outcome<Unit>> { continuation ->
            setup.run(
                onDone = { if (continuation.isActive) continuation.resume(Outcome.Success(Unit)) },
                onFailed = { message ->
                    if (continuation.isActive) continuation.resume(Outcome.Failure(DomainError.Unexpected(message)))
                },
            )
        }
        if (outcome is Outcome.Success) repository.markComplete()
        return outcome
    }
}
