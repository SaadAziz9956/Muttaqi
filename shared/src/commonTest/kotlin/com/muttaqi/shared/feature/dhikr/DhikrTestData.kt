package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStep
import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import kotlinx.datetime.LocalDate

internal object DhikrTestData {
    val book = """
        {"source":"test","sections":[
          {"id":"tasbih","title":"Tasbih","subtitle":"Glorifying Allah","dhikr":[
            {"id":"subhanallah","arabic":"سُبْحَانَ اللَّهِ","transliteration":"SubhanAllah",
             "translation":{"en":"glory be to Allah","ur":"اللہ پاک ہے"},"count":3,
             "hadith":{"en":"Whoever says it…"},"credit":{"en":"HadeethEnc.com","ur":"HadeethEnc.com"},
             "reference":"Sahih Muslim 2695","grade":"Sahih"},
            {"id":"open-ended","arabic":"أَسْتَغْفِرُ اللَّهَ","transliteration":"Astaghfirullah",
             "reference":"Sahih al-Bukhari 6307","grade":"Sahih"}]},
          {"id":"after-prayer","title":"After Prayer","subtitle":"Counted sets","dhikr":[
            {"id":"set","title":"After every prayer · 2, 2 and 1","arabic":"سُبْحَانَ اللَّهِ، الْحَمْدُ لِلَّهِ، اللَّهُ أَكْبَرُ",
             "transliteration":"SubhanAllah x2, Alhamdulillah x2, Allahu Akbar x1","steps":[
               {"arabic":"سُبْحَانَ اللَّهِ","transliteration":"SubhanAllah","translation":{"en":"glory be to Allah","ur":"اللہ پاک ہے"},"count":2},
               {"arabic":"الْحَمْدُ لِلَّهِ","transliteration":"Alhamdulillah","translation":{"en":"praise be to Allah"},"count":2},
               {"arabic":"اللَّهُ أَكْبَرُ","transliteration":"Allahu Akbar","translation":{"en":"Allah is the Most Great"},"count":1}],
             "reference":"Sahih Muslim 597","grade":"Sahih"}]}]}
    """.trimIndent()

    val files = mapOf("Dhikr.json" to book)

    val single = Dhikr("subhanallah", null, "سُبْحَانَ اللَّهِ", "SubhanAllah", "glory be to Allah", emptyList(), 3, null, "Sahih Muslim 2695", "Sahih", null)

    val openEnded = single.copy(id = "open-ended", count = null)

    val set = single.copy(
        id = "set",
        title = "After every prayer · 2, 2 and 1",
        translation = null,
        count = null,
        steps = listOf(
            DhikrStep("سُبْحَانَ اللَّهِ", "SubhanAllah", "glory be to Allah", 2),
            DhikrStep("الْحَمْدُ لِلَّهِ", "Alhamdulillah", "praise be to Allah", 2),
            DhikrStep("اللَّهُ أَكْبَرُ", "Allahu Akbar", null, 1),
        ),
    )

    val today = LocalDate(2026, 9, 30)
}

internal class FakeCurrentDay(var day: LocalDate = DhikrTestData.today) : CurrentDay {
    override fun today(): LocalDate = day
}
