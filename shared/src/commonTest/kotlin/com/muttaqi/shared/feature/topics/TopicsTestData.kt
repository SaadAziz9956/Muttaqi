package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.feature.dua.DuaTestData
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.repository.BundledEmotionRepository
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.CoroutineDispatcher

/** Small Emotions.json and Explore.json, with Hisn al-Muslim duas 1–3 from the Dua tests */
internal object TopicsTestData {
    val emotions = """
        {"source":"test","header":{"reference":"65:3","translation":{"en":"And whoever relies upon Allah - then He is sufficient for him.","ur":"اور جو خدا پر بھروسہ رکھے گا تو وہ اس کو کفایت کرے گا۔"}},
         "emotions":[
          {"id":"angry","title":"Angry",
           "verses":[{"reference":"3:134","arabic":"وَٱلْكَٰظِمِينَ ٱلْغَيْظَ","translation":{"en":"and who restrain anger","ur":"اور غصے کو روکتے"}},
                     {"reference":"7:199","arabic":"خُذِ ٱلْعَفْوَ","translation":{"en":"Take what is given freely"}}],
           "hadith":[{"arabic":"«لَا تَغْضَبْ»","translation":{"en":"Do not get angry.","ur":"غصہ مت کیا کرو"},"attribution":{"en":"Narrated by Al-Bukhāri","ur":"رواه البخاري"},"grade":{"en":"Authentic","ur":"صحيح"},"source":"HadeethEnc.com"}],
           "duas":[1, 99]},
          {"id":"bored","title":"Bored",
           "verses":[{"reference":"94:7","arabic":"فَإِذَا فَرَغْتَ فَٱنصَبْ","translation":{"en":"So when you have finished, then stand up","ur":"تو جب فارغ ہوا کرو تو محنت کیا کرو"}}],
           "hadith":[{"arabic":"نِعْمَتَانِ","translation":{"en":"There are two blessings","ur":"دو نعمتیں"},"attribution":{"en":"Narrated by Al-Bukhāri","ur":"رواه البخاري"},"grade":{"en":"Authentic","ur":"صحيح"},"source":"HadeethEnc.com"}],
           "duas":[]},
          {"id":"happy","title":"Happy","verses":[],"hadith":[],"duas":[2]}]}
    """.trimIndent()

    val explore = """
        {"source":"test","header":{"reference":"29:69","translation":{"en":"And those who strive for Us - We will surely guide them to Our ways.","ur":"اور جن لوگوں نے ہمارے لئے کوشش کی ہم اُن کو ضرور اپنے رستے دکھا دیں گے۔"}},
         "groups":[
          {"id":"worship","title":"Worship","topics":[
            {"id":"fasting","title":"Fasting","icon":"sun-fog-linear","keywords":["sawm","roza"],
             "verses":[{"reference":"2:183","arabic":"كُتِبَ عَلَيْكُمُ ٱلصِّيَامُ","translation":{"en":"decreed upon you is fasting","ur":"تم پر روزے فرض کئے گئے ہیں"}}],
             "hadith":[{"arabic":"مَنْ صَامَ رَمَضَانَ","translation":{"en":"Whoever fasts Ramadan","ur":"جس نے رمضان کے روزے رکھے"},"attribution":{"en":"Agreed upon","ur":"متفق عليه"},"grade":{"en":"Authentic","ur":"صحيح"},"source":"HadeethEnc.com"},
                       {"arabic":"","translation":{"en":"%LONG%"},"attribution":{"en":"Agreed upon"},"grade":{"en":"Authentic"},"source":"HadeethEnc.com"}],
             "duas":[3]},
            {"id":"charity-zakat","title":"Charity & Zakat","icon":"money-send-linear","keywords":["zakah","sadaqah"],
             "verses":[{"reference":"2:43","arabic":"وَأَقِيمُوا۟ ٱلصَّلَوٰةَ وَءَاتُوا۟ ٱلزَّكَوٰةَ","translation":{"en":"And establish prayer and give zakah","ur":"اور نماز پڑھا کرو اور زکوٰۃ دیا کرو"}}],
             "hadith":[{"arabic":"","translation":{"en":"%MARKED%"},"attribution":{"en":"Narrated by Muslim"},"grade":{"en":"Authentic"},"source":"HadeethEnc.com"}],
             "duas":[]},
            {"id":"prayer","title":"Prayer","icon":"clock-linear","keywords":["salah","namaz"],
             "verses":[],"hadith":[],"duas":[1]}]},
          {"id":"sins","title":"Sins to Avoid","topics":[
            {"id":"lying","title":"Lying","icon":"message-remove-linear","keywords":["jhoot"],
             "verses":[{"reference":"40:28","arabic":"إِنَّ ٱللَّهَ لَا يَهْدِى","translation":{"en":"Indeed, Allah does not guide one who is a transgressor and a liar"}}],
             "hadith":[{"arabic":"","translation":{"en":"Truthfulness leads to righteousness"},"attribution":{"en":"Agreed upon"},"grade":{"en":"Authentic"},"source":"HadeethEnc.com"}],
             "duas":[]}]}]}
    """.trimIndent()
        // Too long for Home's card
        .replace("%LONG%", "a".repeat(421))
        // 420 letters, each with a mark on it, which don't count towards the length
        .replace("%MARKED%", "بَ".repeat(420))

    val files = DuaTestData.files + mapOf("Emotions.json" to emotions, "Explore.json" to explore)

    private fun duaEntries(dispatcher: CoroutineDispatcher) =
        GetDuaEntriesById(BundledDuaRepository(FakeContentSource(files), TestDispatchers(dispatcher)))

    fun emotionRepository(dispatcher: CoroutineDispatcher) =
        BundledEmotionRepository(FakeContentSource(files), TestDispatchers(dispatcher), duaEntries(dispatcher))

    fun exploreRepository(dispatcher: CoroutineDispatcher) =
        BundledExploreRepository(FakeContentSource(files), TestDispatchers(dispatcher), duaEntries(dispatcher))
}
