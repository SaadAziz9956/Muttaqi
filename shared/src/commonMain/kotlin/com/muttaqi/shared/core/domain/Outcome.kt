package com.muttaqi.shared.core.domain

sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: DomainError) : Outcome<Nothing>
}

sealed interface DomainError {
    data object NoConnection : DomainError
    data object NotFound : DomainError
    data class Unexpected(val message: String?) : DomainError
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}
