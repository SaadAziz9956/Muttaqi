package com.muttaqi.shared.core.domain

import kotlinx.datetime.LocalDate

val LocalDate.dayOfEra: Long get() = toEpochDays() + DAYS_BEFORE_1970

private const val DAYS_BEFORE_1970 = 719_163L
