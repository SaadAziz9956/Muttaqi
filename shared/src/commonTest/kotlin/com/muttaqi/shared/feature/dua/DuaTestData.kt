package com.muttaqi.shared.feature.dua

internal object DuaTestData {
    val quranic = """
        [{"surah":2,"ayah":201,"arabic":"رَبَّنَآ ءَاتِنَا","transliteration":"Rabbanaa aatina","translations":{"en":"Our Lord, give us","ur":"اے پروردگار ہم کو"}},
         {"surah":3,"ayah":8,"arabic":"رَبَّنَا لَا تُزِغْ","transliteration":"Rabbanaa laa tuzigh","translations":{"en":"Our Lord, let not our hearts deviate"}}]
    """.trimIndent()

    val hisn = """
        {"source":"test","categories":[
          {"id":"sleep","title":"Sleep & Waking","chapters":[
            {"id":1,"title":"Upon waking up","titleArabic":"أذكار الاستيقاظ","duas":[
              {"id":1,"arabic":"الْحَمْدُ للهِ","transliteration":"Alhamdu lillaah","translation":"Praise is to Allah","translationUrdu":"سب تعریف اللہ کے لیے","translationUrduCredit":"Za'i","repeat":0,"reference":"البخاري","source":"Bukhari"}]},
            {"id":2,"title":"Before sleeping","titleArabic":"أذكار النوم","duas":[
              {"id":2,"arabic":"بِاسْمِكَ اللَّهُمَّ","transliteration":"Bismika Allahumma","translation":"In Your name, O Allah","repeat":3,"reference":"","source":"Bukhari · Muslim"}]}]},
          {"id":"travel","title":"Travel","chapters":[
            {"id":3,"title":"The travel dua","titleArabic":"دعاء السفر","duas":[
              {"id":3,"arabic":"سُبْحَانَ الَّذِي سَخَّرَ","transliteration":"Subhaanal-lathee","translation":"How perfect He is","repeat":1,"reference":"مسلم","source":"Muslim"}]}]}]}
    """.trimIndent()

    val files = mapOf("Duas.json" to quranic, "HisnAlMuslim.json" to hisn)
}
