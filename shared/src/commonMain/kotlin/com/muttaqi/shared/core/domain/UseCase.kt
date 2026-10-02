package com.muttaqi.shared.core.domain

import kotlinx.coroutines.flow.Flow

fun interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

fun interface NoParamsUseCase<out R> {
    suspend operator fun invoke(): R
}

fun interface FlowUseCase<in P, out R> {
    operator fun invoke(params: P): Flow<R>
}
