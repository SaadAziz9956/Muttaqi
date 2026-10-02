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

class IsOnboardingComplete(private val repository: OnboardingRepository) {
    operator fun invoke(): Boolean = repository.isComplete()
}

class SaveUserName(private val repository: OnboardingRepository) {
    operator fun invoke(name: String): Boolean {
        if (name.isBlank()) return false
        repository.saveUserName(name)
        return true
    }
}

class RequestNotificationPermission(private val permission: NotificationPermission) {
    suspend operator fun invoke(): Boolean = suspendCancellableCoroutine { continuation ->
        permission.request { granted -> if (continuation.isActive) continuation.resume(granted) }
    }
}

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
