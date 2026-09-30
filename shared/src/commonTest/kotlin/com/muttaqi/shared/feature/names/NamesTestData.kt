package com.muttaqi.shared.feature.names

internal object NamesTestData {
    val book = """
        {"source":"test","names":[
          {"number":1,"arabic":"اللَّهُ","transliteration":"Allāh","meaning":{"en":"He is Allah","ur":"اسم ذات"}},
          {"number":2,"arabic":"الرَّحْمَنُ","transliteration":"Ar-Raḥmān","meaning":{"en":"the Most Merciful (to the creation)","ur":"بہت رحم کرنے والا"}},
          {"number":3,"arabic":"الرَّحِيمُ","transliteration":"Ar-Raḥīm","meaning":{"en":"the Most Beneficent (to the believers)"}},
          {"number":4,"arabic":"الْغَفُورُ","transliteration":"Al-Ghafoor","meaning":{"en":"the All-Forgiving","ur":"بخشنے والا"}}]}
    """.trimIndent()

    val files = mapOf("AsmaUlHusna.json" to book)
}
