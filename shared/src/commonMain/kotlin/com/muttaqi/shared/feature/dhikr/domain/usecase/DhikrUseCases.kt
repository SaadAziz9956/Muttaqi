package com.muttaqi.shared.feature.dhikr.domain.usecase

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.model.counted
import com.muttaqi.shared.feature.dhikr.domain.model.repetitionsSaid
import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrRepository

class GetDhikrSections(private val repository: DhikrRepository) {
    suspend operator fun invoke(language: Language): List<DhikrSection> = repository.sections(language)
}

class GetDhikr(private val repository: DhikrRepository) {
    suspend operator fun invoke(id: String, language: Language): Dhikr? =
        repository.sections(language).asSequence().flatMap { it.dhikr }.firstOrNull { it.id == id }
}

class GetTodaysDhikrProgress(private val progress: DhikrProgressRepository, private val currentDay: CurrentDay) {
    operator fun invoke(dhikrId: String): DhikrProgress {
        val today = currentDay.today()
        return progress.saved(dhikrId)?.takeIf { it.day == today } ?: DhikrProgress.empty(today)
    }
}

class CountDhikr(private val progress: DhikrProgressRepository, private val currentDay: CurrentDay) {
    operator fun invoke(dhikr: Dhikr, current: DhikrProgress): DhikrProgress =
        current.counted(dhikr, currentDay.today()).also { progress.save(dhikr.id, it) }
}

class ResetDhikrProgress(private val progress: DhikrProgressRepository, private val currentDay: CurrentDay) {
    operator fun invoke(dhikrId: String): DhikrProgress =
        DhikrProgress.empty(currentDay.today()).also { progress.save(dhikrId, it) }
}

class GetDhikrSaidToday(
    private val repository: DhikrRepository,
    private val getProgress: GetTodaysDhikrProgress,
) {
    suspend operator fun invoke(): Int = repository.sections(Language.English)
        .flatMap { it.dhikr }
        .sumOf { dhikr -> getProgress(dhikr.id).repetitionsSaid(dhikr) }
}
