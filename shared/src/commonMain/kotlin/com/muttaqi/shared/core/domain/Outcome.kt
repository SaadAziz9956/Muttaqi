package com.muttaqi.shared.core.domain

/** The result of work that can fail in a way the screen should explain, e.g. no connection when syncing the Quran */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: DomainError) : Outcome<Nothing>
}

/** Why something failed, in terms the app can act on rather than as a platform exception */
sealed interface DomainError {
    data object NoConnection : DomainError
    data object NotFound : DomainError
    data class Unexpected(val message: String?) : DomainError
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}
