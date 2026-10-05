package com.muttaqi.shared.core.quote

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.model.inLanguage

data class PublishedQuote(
    private val translations: Map<String, String>,
    val source: String,
) {
    fun text(language: Language): String = translations.inLanguage(language)
}

object PageQuotes {
    val doNotWeaken = PublishedQuote(
        mapOf(
            "en" to "So do not weaken and do not grieve, and you will be superior if you are [true] believers.",
            "ur" to "اور (دیکھو) بے دل نہ ہونا اور نہ کسی طرح کا غم کرنا اگر تم مومن (صادق) ہو تو تم ہی غالب رہو گے",
        ),
        "Quran (3:139)",
    )

    val callUponMe = PublishedQuote(
        mapOf(
            "en" to "And your Lord says, \"Call upon Me; I will respond to you.\" Indeed, those who disdain My worship will enter Hell [rendered] contemptible.",
            "ur" to "اور تمہارے پروردگار نے کہا ہے کہ تم مجھ سے دعا کرو میں تمہاری (دعا) قبول کروں گا۔ جو لوگ میری عبادت سے ازراہ تکبر کنیاتے ہیں۔ عنقریب جہنم میں ذلیل ہو کر داخل ہوں گے",
        ),
        "Quran (40:60)",
    )

    val byThePen = PublishedQuote(
        mapOf(
            "en" to "Nun. By the pen and what they inscribe,",
            "ur" to "نٓ۔ قلم کی اور جو (اہل قلم) لکھتے ہیں اس کی قسم",
        ),
        "Quran (68:1)",
    )

    val inviteWithWisdom = PublishedQuote(
        mapOf(
            "en" to "Invite to the way of your Lord with wisdom and good instruction, and argue with them in a way that is best.",
            "ur" to "(اے پیغمبر) لوگوں کو دانش اور نیک نصیحت سے اپنے پروردگار کے رستے کی طرف بلاؤ۔ اور بہت ہی اچھے طریق سے ان سے مناظرہ کرو۔",
        ),
        "Quran (16:125)",
    )

    val learnAndTeachQuran = PublishedQuote(
        mapOf(
            "en" to "The best of you are those who learn the Qur’an and teach it.",
            "ur" to "تم میں سب سے بہتر شخص وہ ہے جو قرآن سیکھے اور اسے سکھائے",
        ),
        "Sahih al-Bukhari 5027 · HadeethEnc.com",
    )

    val ninetyNineNames = PublishedQuote(
        mapOf(
            "en" to "Verily, Allah has ninety-nine names, one-hundred minus one. Whoever memorizes them all will enter Paradise.",
            "ur" to "اللہ کے ننانوے یعنی ایک کم ایک سو نام ہیں، جو ان کی حفاظت کرے گا، وہ جنت میں داخل ہوگا",
        ),
        "Sahih al-Bukhari 7392, Sahih Muslim 2677 · HadeethEnc.com",
    )

    val rememberingAllah = PublishedQuote(
        mapOf(
            "en" to "The example of the one who remembers his Lord and the one who does not remember His Lord is like the example of the living and the dead person.",
            "ur" to "اس شخص کی مثال جو اپنے رب کو یاد کرتا ہے اور جو اسے یاد نہیں کرتا، زندہ اور مردہ کی سی ہے",
        ),
        "Sahih al-Bukhari 6407 · HadeethEnc.com",
    )
}

data class DisplayedQuote(val text: String, val source: String)

fun PublishedQuote.displayed(language: Language) = DisplayedQuote(text(language), source)
