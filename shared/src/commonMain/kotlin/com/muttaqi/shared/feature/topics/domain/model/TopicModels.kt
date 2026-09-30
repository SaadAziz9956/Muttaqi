package com.muttaqi.shared.feature.topics.domain.model

import com.muttaqi.shared.feature.dua.domain.model.DuaEntry

/** A Quran verse, or a few in a row, as a topic page shows it, in the reader's language */
data class QuranPassage(
    /** e.g. "3:134", or "59:22-24" for verses in a row */
    val reference: String,
    val arabic: String,
    /** Published translation, word for word */
    val translation: String,
    /** Whose translation it is, e.g. "Saheeh International" */
    val credit: String,
) {
    /** e.g. "Quran (3:134)" */
    val source: String get() = "Quran ($reference)"
}

/** An authentic hadith as a topic page shows it, in the reader's language */
data class HadithPassage(
    val arabic: String,
    /** Published translation, in full as the publisher asks */
    val translation: String,
    /** Who narrated it in the collections, e.g. "Agreed upon" */
    val attribution: String,
    val grade: String,
    /** Whose translation it is, e.g. "HadeethEnc.com" */
    val credit: String,
) {
    /** e.g. "Narrated by Al-Bukhāri · Authentic" */
    val source: String get() = "$attribution · $grade"
}

/** Anything shown as a page of Quran verses, hadith and duas: an emotion, or an Explore topic */
interface PassageTopic {
    val id: String
    val title: String
    val verses: List<QuranPassage>
    val hadith: List<HadithPassage>
    /** From Hisn al-Muslim */
    val duas: List<DuaEntry>
}

/** A feeling, e.g. Anxious, with the verses, authentic hadith and duas that speak to it */
data class Emotion(
    override val id: String,
    override val title: String,
    override val verses: List<QuranPassage>,
    override val hadith: List<HadithPassage>,
    override val duas: List<DuaEntry>,
) : PassageTopic

/** A subject in Explore, e.g. Fasting or Honesty, with the verses, authentic hadith and duas about it */
data class ExploreTopic(
    override val id: String,
    override val title: String,
    /** Iconsax icon, e.g. "drop-linear": in the iOS asset catalogue, and `ic_drop_linear` on Android */
    val icon: String,
    /** Other words a reader might search for, e.g. "zakah" and "sadaqah" for Charity & Zakat */
    val keywords: List<String>,
    override val verses: List<QuranPassage>,
    override val hadith: List<HadithPassage>,
    override val duas: List<DuaEntry>,
) : PassageTopic

/** A heading on the Explore page, e.g. Worship, and its topics */
data class ExploreGroup(
    val id: String,
    val title: String,
    val topics: List<ExploreTopic>,
)

/** Which topics a topic page's chips move between */
enum class TopicChips {
    /** Every emotion */
    Emotions,

    /** The topics in the Explore group of the topic that was opened */
    ExploreGroup,
}
