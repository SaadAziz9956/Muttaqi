package com.muttaqi.android.feature.home

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Surah

/**
 * The Quran as Home shows it on 30 September and 2 October 2026, word for word from the Quran API's editions (Uthmani,
 * Saheeh International, Fateh Muhammad Jalandhry) as the app stores them: the verse under the greeting (3:139) and
 * the Ayah of the Day (16:128, and 25:63 on the Friday), with the surahs Home names
 */
internal object HomeScreenshotQuran {
    val surahs = listOf(
        Surah(2, "سورة البقرة", "Al-Baqara", "The Cow", "Medinan", 286),
        Surah(3, "سورة آل عمران", "Aal-i-Imraan", "The Family of Imraan", "Medinan", 200),
        Surah(16, "سورة النحل", "An-Nahl", "The Bee", "Meccan", 128),
        Surah(18, "سورة الكهف", "Al-Kahf", "The Cave", "Meccan", 110),
        Surah(25, "سورة الفرقان", "Al-Furqaan", "The Criterion", "Meccan", 77),
    )

    private class Text(val ayah: Ayah, val english: String, val urdu: String)

    private val texts = listOf(
        Text(
            Ayah(432, 139, 3, "وَلَا تَهِنُوا۟ وَلَا تَحْزَنُوا۟ وَأَنتُمُ ٱلْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ", "Wa laa tahinoo wa laa tahzanoo wa antumul a'lawna in kuntum mu'mineen", null, 4, 67, 27),
            english = "So do not weaken and do not grieve, and you will be superior if you are [true] believers.",
            urdu = "اور (دیکھو) بے دل نہ ہونا اور نہ کسی طرح کا غم کرنا اگر تم مومن (صادق) ہو تو تم ہی غالب رہو گے",
        ),
        Text(
            Ayah(2029, 128, 16, "إِنَّ ٱللَّهَ مَعَ ٱلَّذِينَ ٱتَّقَوا۟ وَّٱلَّذِينَ هُم مُّحْسِنُونَ", "Innal laaha ma'al lazeenat taqaw wal lazeena hum muhsinoon", null, 14, 281, 112),
            english = "Indeed, Allah is with those who fear Him and those who are doers of good.",
            urdu = "کچھ شک نہیں کہ جو پرہیزگار ہیں اور جو نیکوکار ہیں خدا ان کا مددگار ہے",
        ),
        Text(
            Ayah(2918, 63, 25, "وَعِبَادُ ٱلرَّحْمَٰنِ ٱلَّذِينَ يَمْشُونَ عَلَى ٱلْأَرْضِ هَوْنًۭا وَإِذَا خَاطَبَهُمُ ٱلْجَٰهِلُونَ قَالُوا۟ سَلَٰمًۭا", "Wa 'ibaadur Rahmaanil lazeena yamshoona 'alal ardi hawnanw wa izaa khaata bahumul jaahiloona qaaloo salaamaa", null, 19, 365, 146),
            english = "And the servants of the Most Merciful are those who walk upon the earth easily, and when the ignorant address them [harshly], they say [words of] peace,",
            urdu = "اور خدا کے بندے تو وہ ہیں جو زمین پر آہستگی سے چلتے ہیں اور جب جاہل لوگ ان سے (جاہلانہ) گفتگو کرتے ہیں تو سلام کہتے ہیں",
        ),
    )

    fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah? =
        texts.firstOrNull { it.ayah.surahNumber == surahNumber && it.ayah.numberInSurah == numberInSurah }?.let {
            it.ayah.copy(translation = if (language == Language.Urdu) it.urdu else it.english)
        }
}
