package com.muttaqi.shared.feature.topics.domain.model

import com.muttaqi.shared.feature.dua.domain.model.DuaEntry

data class QuranPassage(
    val reference: String,
    val arabic: String,
    val translation: String,
    val credit: String,
) {
    val source: String get() = "Quran ($reference)"
}

data class HadithPassage(
    val arabic: String,
    val translation: String,
    val attribution: String,
    val grade: String,
    val credit: String,
) {
    val source: String get() = "$attribution · $grade"
}

interface PassageTopic {
    val id: String
    val title: String
    val verses: List<QuranPassage>
    val hadith: List<HadithPassage>
    val duas: List<DuaEntry>
}

data class Emotion(
    override val id: String,
    override val title: String,
    override val verses: List<QuranPassage>,
    override val hadith: List<HadithPassage>,
    override val duas: List<DuaEntry>,
) : PassageTopic

data class ExploreTopic(
    override val id: String,
    override val title: String,
    val icon: String,
    val keywords: List<String>,
    override val verses: List<QuranPassage>,
    override val hadith: List<HadithPassage>,
    override val duas: List<DuaEntry>,
) : PassageTopic

data class ExploreGroup(
    val id: String,
    val title: String,
    val topics: List<ExploreTopic>,
)

enum class TopicChips {
    Emotions,

    ExploreGroup,
}
