package com.muttaqi.shared.feature.onboarding.domain.usecase

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.repository.OnboardingRepository
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import kotlinx.coroutines.CancellationException
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

/**
 * The first launch's download, the Quran's Arabic, transliteration and English translation, which English becomes
 * the reading language with; once it's done, onboarding is complete
 */
class FinishOnboarding(private val syncQuran: SyncQuran, private val repository: OnboardingRepository) {
    suspend operator fun invoke(): Outcome<Unit> {
        val outcome = try {
            syncQuran(Language.English)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Outcome.Failure(DomainError.Unexpected(failure.message))
        }
        if (outcome is Outcome.Success) repository.markComplete()
        return outcome
    }
}
