package com.muttaqi.shared.core.domain

import kotlinx.coroutines.flow.Flow

/**
 * One thing the app can do, e.g. get the Explore topics. Each use case is its own small interface, so a view model
 * depends only on what it uses, and a test can replace it with a lambda
 */
fun interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/** A use case that needs nothing passed in, e.g. the day's Name of Allah */
fun interface NoParamsUseCase<out R> {
    suspend operator fun invoke(): R
}

/** A use case whose result keeps changing, e.g. the journal's entries as they're edited */
fun interface FlowUseCase<in P, out R> {
    operator fun invoke(params: P): Flow<R>
}
