package com.muttaqi.shared.core.domain

import kotlinx.datetime.LocalDate

/**
 * The day's number in the Common Era, 1 January of year 1 being day 1. The iOS app has always numbered its days this
 * way for the picks of the day (Foundation's `ordinality(of: .day, in: .era)` in the Gregorian calendar), so a reader
 * sees the same Name, topic, hadith, ayah and dua on the same day
 */
val LocalDate.dayOfEra: Long get() = toEpochDays() + DAYS_BEFORE_1970

/** 1970-01-01 is day 719,163 of the Common Era */
private const val DAYS_BEFORE_1970 = 719_163L
