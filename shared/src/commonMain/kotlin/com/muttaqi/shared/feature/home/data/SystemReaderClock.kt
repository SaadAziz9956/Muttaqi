package com.muttaqi.shared.feature.home.data

import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

class SystemReaderClock(private val clock: Clock = Clock.System) : ReaderClock {
    override val timeZone: TimeZone get() = TimeZone.currentSystemDefault()

    override fun now(): Instant = clock.now()

    override fun minutes(): Flow<Instant> = flow {
        while (true) {
            val now = clock.now()
            emit(now)
            delay(MINUTE - now.toEpochMilliseconds().mod(MINUTE))
        }
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
