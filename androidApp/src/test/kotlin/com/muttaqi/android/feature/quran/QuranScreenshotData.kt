package com.muttaqi.android.feature.quran

import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Surah

internal object QuranScreenshotData {
    val surahs = listOf(
        Surah(1, "سورة الفاتحة", "Al-Faatiha", "The Opening", "Meccan", 7),
        Surah(2, "سورة البقرة", "Al-Baqara", "The Cow", "Medinan", 286),
        Surah(3, "سورة آل عمران", "Aal-i-Imraan", "The Family of Imraan", "Medinan", 200),
        Surah(4, "سورة النساء", "An-Nisaa", "The Women", "Medinan", 176),
        Surah(5, "سورة المائدة", "Al-Maaida", "The Table", "Medinan", 120),
        Surah(6, "سورة الأنعام", "Al-An'aam", "The Cattle", "Meccan", 165),
        Surah(9, "سورة التوبة", "At-Tawba", "The Repentance", "Medinan", 129),
        Surah(18, "سورة الكهف", "Al-Kahf", "The Cave", "Meccan", 110),
    )

    val fatiha = listOf(
        Ayah(1, 1, 1, "﻿بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Bismillaahir Rahmaanir Raheem", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", 1, 1, 1),
        Ayah(2, 2, 1, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "Alhamdu lillaahi Rabbil 'aalameen", "[All] praise is [due] to Allah, Lord of the worlds -", 1, 1, 1),
        Ayah(3, 3, 1, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Ar-Rahmaanir-Raheem", "The Entirely Merciful, the Especially Merciful,", 1, 1, 1),
        Ayah(4, 4, 1, "مَٰلِكِ يَوْمِ ٱلدِّينِ", "Maaliki Yawmid-Deen", "Sovereign of the Day of Recompense.", 1, 1, 1),
        Ayah(5, 5, 1, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaaka na'budu wa lyyaaka nasta'een", "It is You we worship and You we ask for help.", 1, 1, 1),
        Ayah(6, 6, 1, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Ihdinas-Siraatal-Mustaqeem", "Guide us to the straight path -", 1, 1, 1),
        Ayah(7, 7, 1, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "Siraatal-lazeena an'amta 'alaihim ghayril-maghdoobi 'alaihim wa lad-daaalleen", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", 1, 1, 1),
    )

    val baqara = listOf(
        Ayah(8, 1, 2, "الٓمٓ", "Alif-Laaam-Meeem", "Alif, Lam, Meem.", 1, 2, 1),
        Ayah(9, 2, 2, "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًۭى لِّلْمُتَّقِينَ", "Zaalikal Kitaabu laa raiba feeh; udal lilmuttaqeen", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", 1, 2, 1),
        Ayah(10, 3, 2, "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَٰهُمْ يُنفِقُونَ", "Allazeena yu'minoona bilghaibi wa yuqeemoonas salaata wa mimmaa razaqnaahum yunfiqoon", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", 1, 2, 1),
        Ayah(11, 4, 2, "وَٱلَّذِينَ يُؤْمِنُونَ بِمَآ أُنزِلَ إِلَيْكَ وَمَآ أُنزِلَ مِن قَبْلِكَ وَبِٱلْءَاخِرَةِ هُمْ يُوقِنُونَ", "Wallazeena yu'minoona bimaa unzila ilaika wa maaa unzila min qablika wa bil Aakhirati hum yooqinoon", "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].", 1, 2, 1),
        Ayah(12, 5, 2, "أُو۟لَٰٓئِكَ عَلَىٰ هُدًۭى مِّن رَّبِّهِمْ ۖ وَأُو۟لَٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ", "Ulaaa'ika 'alaa hudam mir rabbihim wa ulaaa'ika humul muflihoon", "Those are upon [right] guidance from their Lord, and it is those who are the successful.", 1, 2, 1),
        Ayah(13, 6, 2, "إِنَّ ٱلَّذِينَ كَفَرُوا۟ سَوَآءٌ عَلَيْهِمْ ءَأَنذَرْتَهُمْ أَمْ لَمْ تُنذِرْهُمْ لَا يُؤْمِنُونَ", "Innal lazeena kafaroo sawaaa'un 'alaihim 'a-anzar tahum am lam tunzirhum laa yu'minoon", "Indeed, those who disbelieve - it is all the same for them whether you warn them or do not warn them - they will not believe.", 1, 3, 1),
        Ayah(14, 7, 2, "خَتَمَ ٱللَّهُ عَلَىٰ قُلُوبِهِمْ وَعَلَىٰ سَمْعِهِمْ ۖ وَعَلَىٰٓ أَبْصَٰرِهِمْ غِشَٰوَةٌۭ ۖ وَلَهُمْ عَذَابٌ عَظِيمٌۭ", "Khatamal laahu 'alaa quloobihim wa 'alaa sam'i-him wa 'alaaa absaarihim ghishaa watunw wa lahum 'azaabun 'azeem", "Allah has set a seal upon their hearts and upon their hearing, and over their vision is a veil. And for them is a great punishment.", 1, 3, 1),
        Ayah(15, 8, 2, "وَمِنَ ٱلنَّاسِ مَن يَقُولُ ءَامَنَّا بِٱللَّهِ وَبِٱلْيَوْمِ ٱلْءَاخِرِ وَمَا هُم بِمُؤْمِنِينَ", "Wa minan naasi mai yaqoolu aamannaa billaahi wa bil yawmil aakhiri wa maa hum bimu'mineen", "And of the people are some who say, \"We believe in Allah and the Last Day,\" but they are not believers.", 1, 3, 1),
        Ayah(16, 9, 2, "يُخَٰدِعُونَ ٱللَّهَ وَٱلَّذِينَ ءَامَنُوا۟ وَمَا يَخْدَعُونَ إِلَّآ أَنفُسَهُمْ وَمَا يَشْعُرُونَ", "Yukhaadi'oonal laaha wallazeena aamanoo wa maa yakhda'oona illaaa anfusahum wa maa yash'uroon", "They [think to] deceive Allah and those who believe, but they deceive not except themselves and perceive [it] not.", 1, 3, 1),
        Ayah(17, 10, 2, "فِى قُلُوبِهِم مَّرَضٌۭ فَزَادَهُمُ ٱللَّهُ مَرَضًۭا ۖ وَلَهُمْ عَذَابٌ أَلِيمٌۢ بِمَا كَانُوا۟ يَكْذِبُونَ", "Fee quloobihim mara dun fazzdahumul laahu maradan wa lahum 'azaabun aleemum bimaa kaanoo yakziboon", "In their hearts is disease, so Allah has increased their disease; and for them is a painful punishment because they [habitually] used to lie.", 1, 3, 1),
        Ayah(18, 11, 2, "وَإِذَا قِيلَ لَهُمْ لَا تُفْسِدُوا۟ فِى ٱلْأَرْضِ قَالُوٓا۟ إِنَّمَا نَحْنُ مُصْلِحُونَ", "Wa izaa qeela lahum laa tufsidoo fil ardi qaalo innamaa nahnu muslihoon", "And when it is said to them, \"Do not cause corruption on the earth,\" they say, \"We are but reformers.\"", 1, 3, 1),
        Ayah(19, 12, 2, "أَلَآ إِنَّهُمْ هُمُ ٱلْمُفْسِدُونَ وَلَٰكِن لَّا يَشْعُرُونَ", "Alaaa innahum humul mufsidoona wa laakil laa yash'uroon", "Unquestionably, it is they who are the corrupters, but they perceive [it] not.", 1, 3, 1),
        Ayah(20, 13, 2, "وَإِذَا قِيلَ لَهُمْ ءَامِنُوا۟ كَمَآ ءَامَنَ ٱلنَّاسُ قَالُوٓا۟ أَنُؤْمِنُ كَمَآ ءَامَنَ ٱلسُّفَهَآءُ ۗ أَلَآ إِنَّهُمْ هُمُ ٱلسُّفَهَآءُ وَلَٰكِن لَّا يَعْلَمُونَ", "Wa izaa qeela lahum aaminoo kamaaa aamanan naasu qaalooo anu'minu kamaaa aamanas sufahaaa'; alaaa innahum humus sufahaaa'u wa laakil laa ya'lamoon", "And when it is said to them, \"Believe as the people have believed,\" they say, \"Should we believe as the foolish have believed?\" Unquestionably, it is they who are the foolish, but they know [it] not.", 1, 3, 1),
        Ayah(21, 14, 2, "وَإِذَا لَقُوا۟ ٱلَّذِينَ ءَامَنُوا۟ قَالُوٓا۟ ءَامَنَّا وَإِذَا خَلَوْا۟ إِلَىٰ شَيَٰطِينِهِمْ قَالُوٓا۟ إِنَّا مَعَكُمْ إِنَّمَا نَحْنُ مُسْتَهْزِءُونَ", "Wa izaa laqul lazeena aamanoo qaalooo aamannaa wa izaa khalw ilaa shayaateenihim qaalooo innaa ma'akum innamaa nahnu mustahzi'oon", "And when they meet those who believe, they say, \"We believe\"; but when they are alone with their evil ones, they say, \"Indeed, we are with you; we were only mockers.\"", 1, 3, 1),
        Ayah(22, 15, 2, "ٱللَّهُ يَسْتَهْزِئُ بِهِمْ وَيَمُدُّهُمْ فِى طُغْيَٰنِهِمْ يَعْمَهُونَ", "Allahu yastahzi'u bihim wa yamudduhum fee tughyaanihim ya'mahoon", "[But] Allah mocks them and prolongs them in their transgression [while] they wander blindly.", 1, 3, 1),
        Ayah(23, 16, 2, "أُو۟لَٰٓئِكَ ٱلَّذِينَ ٱشْتَرَوُا۟ ٱلضَّلَٰلَةَ بِٱلْهُدَىٰ فَمَا رَبِحَت تِّجَٰرَتُهُمْ وَمَا كَانُوا۟ مُهْتَدِينَ", "Ulaaa'ikal lazeenash tara wud dalaalata bilhudaa famaa rabihat tijaaratuhum wa maa kaanoo muhtadeen", "Those are the ones who have purchased error [in exchange] for guidance, so their transaction has brought no profit, nor were they guided.", 1, 3, 1),
    )

    val baqaraUrdu = listOf(
        Ayah(8, 1, 2, "الٓمٓ", "Alif-Laaam-Meeem", "الم", 1, 2, 1),
        Ayah(9, 2, 2, "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًۭى لِّلْمُتَّقِينَ", "Zaalikal Kitaabu laa raiba feeh; udal lilmuttaqeen", "یہ کتاب (قرآن مجید) اس میں کچھ شک نہیں (کہ کلامِ خدا ہے۔ خدا سے) ڈرنے والوں کی رہنما ہے", 1, 2, 1),
        Ayah(10, 3, 2, "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَٰهُمْ يُنفِقُونَ", "Allazeena yu'minoona bilghaibi wa yuqeemoonas salaata wa mimmaa razaqnaahum yunfiqoon", "جو غیب پر ایمان لاتے اور آداب کے ساتھ نماز پڑھتے اور جو کچھ ہم نے ان کو عطا فرمایا ہے اس میں سے خرچ کرتے ہیں", 1, 2, 1),
        Ayah(11, 4, 2, "وَٱلَّذِينَ يُؤْمِنُونَ بِمَآ أُنزِلَ إِلَيْكَ وَمَآ أُنزِلَ مِن قَبْلِكَ وَبِٱلْءَاخِرَةِ هُمْ يُوقِنُونَ", "Wallazeena yu'minoona bimaa unzila ilaika wa maaa unzila min qablika wa bil Aakhirati hum yooqinoon", "اور جو کتاب (اے محمدﷺ) تم پر نازل ہوئی اور جو کتابیں تم سے پہلے (پیغمبروں پر) نازل ہوئیں سب پر ایمان لاتے اور آخرت کا یقین رکھتے ہیں", 1, 2, 1),
        Ayah(12, 5, 2, "أُو۟لَٰٓئِكَ عَلَىٰ هُدًۭى مِّن رَّبِّهِمْ ۖ وَأُو۟لَٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ", "Ulaaa'ika 'alaa hudam mir rabbihim wa ulaaa'ika humul muflihoon", "یہی لوگ اپنے پروردگار (کی طرف) سے ہدایت پر ہیں اور یہی نجات پانے والے ہیں", 1, 2, 1),
    )

    val fatihaTafsir = listOf(
        "Introduction to Fatihah",
        "Which was revealed in Makkah",
        "The Meaning of Al-Fatihah and its Various Names",
        "This Surah is called",
        "- Al-Fatihah, that is, the Opener of the Book, the Surah with which prayers are begun.",
        "- It is also called, Umm Al-Kitab (the Mother of the Book), according to the majority of the scholars.",
        "In an authentic Hadith recorded by At-Tirmidhi, who graded it Sahih, Abu Hurayrah said that the Messenger of Allah ﷺ said,",
        "الْحَمْدُ للهِ رَبَ الْعَالَمِينَ أُمُّ الْقُرْآنِ وَأُمُّ الْكِتَابِ وَالسَّبْعُ الْمَثَانِي وَالْقُرْآنُ الْعَظِيمُ",
        "Al-Hamdu lillahi Rabbil-`Alamin is the Mother of the Qur'an, the Mother of the Book, and the seven repeated Ayat of the Glorious Qur'an.It is also called Al-Hamd and As-Salah, because the Prophet ﷺ said that his Lord said,",
    ).joinToString("\n\n")
    val fatihaTafsir2 = listOf(
        "The Meaning of Al-Hamd",
        "Abu Ja`far bin Jarir said, \"The meaning of",
        "الْحَمْدُ للَّهِ",
    ).joinToString("\n\n")
}
