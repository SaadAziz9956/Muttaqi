import json, os

BASE = os.path.dirname(os.path.abspath(__file__))
HISN = "/Users/vyro/Projects/IOS Projects/Muttaqi/Muttaqi/Resources/Data/HisnAlMuslim.json"


def qsrc(ref):
    return json.load(open(os.path.join(BASE, "quran", ref.replace(":", "_") + ".json")))


def verse(ref, why, cut=None):
    d = qsrc(ref)
    ar, en, ur = d["quran-uthmani"].strip(), d["en.sahih"].strip(), d["ur.jalandhry"].strip()
    item = {"ref": ref, "part": cut is not None}
    if cut:
        car, cen, cur = cut
        if isinstance(car, tuple):
            car = " ".join(ar.split(" ")[car[0]:car[1]])
        for piece, full, lang in ((car, ar, "ar"), (cen, en, "en"), (cur, ur, "ur")):
            assert piece in full, (ref, lang, piece)
        ar, en, ur = car, cen, cur
    item.update({"arabic": ar, "en": en, "ur": ur, "why": why})
    return item


def hadith(hid, why, note=None):
    e = json.load(open(os.path.join(BASE, "he", "en", f"{hid}.json")))
    u = json.load(open(os.path.join(BASE, "he", "ur", f"{hid}.json")))
    assert e["id"] == str(hid) and u["id"] == str(hid)
    item = {
        "hadeethencId": hid,
        "arabic": e["hadeeth_ar"].strip(),
        "en": e["hadeeth"].strip(),
        "ur": u["hadeeth"].strip(),
        "attribution": e["attribution"].strip(),
        "grade": e["grade"].strip(),
        "attributionUr": u["attribution"].strip(),
        "gradeUr": u["grade"].strip(),
        "sourceUrl": f"https://hadeethenc.com/en/browse/hadith/{hid}",
        "why": why,
    }
    if e["hadeeth_ar"].strip() != u["hadeeth_ar"].strip():
        item["arabicUr"] = u["hadeeth_ar"].strip()
        item["note"] = note or "HadeethEnc's Urdu translation was made from a slightly different Arabic wording (given in arabicUr); meaning is the same."
    return item


hisn = json.load(open(HISN))
HISN_BY_ID = {x["id"]: x for c in hisn["categories"] for ch in c["chapters"] for x in ch["duas"]}
DUA_CHECK = {
    193: "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
    121: "الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ",
    128: "حَسْبُنا اللَّهُ وَنِعْمَ الْوَكِيلُ",
    232: "لاَ تُؤَاخِذْنِي بِمَا يَقُولُونَ",
    88: "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغيثُ",
    30: "اهْدِنِي لِمَا اخْتُلِفَ فِيهِ مِنَ الْحَقِّ",
    87: "رَضِيتُ بِاللَّهِ رَبَّاً",
    120: "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ",
    123: "اللَّهُمَّ رَحْمَتَكَ أَرْجُو",
    124: "إِنِّي كُنْتُ مِنَ الظّالِمِينَ",
    133: "يَسْتَعِيذُ بِاللَّهِ",
    134: "آمَنْتُ بِاللَّهِ وَرُسُلِهِ",
    135: "هُوَ الْأوَّلُ وَالْآخِرُ",
    59: "أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ",
    136: "اكْفِنِي بِحَلاَلِكَ عَنْ حَرَامِكَ",
    60: "أَعُوذُ بِكَ مِنَ الْبُخْلِ",
    79: "أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي",
    57: "ظَلَمْتُ نَفْسِي ظُلْماً كَثِيراً",
    140: "يُذنِبُ ذَنْباً فَيُحْسِنُ الطُّهُورَ",
    218: "الْحَمْدُ لِلَّهِ عَلَى كُلِّ حَالٍ",
    144: "قَدَرُ اللَّهُ وَمَا شَاءَ فَعَلَ",
    74: "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ",
    139: "لاَ سَهْلَ إِلاَّ مَا جَعَلْتَهُ سَهْلاً",
    203: "أَعُوذُ بِكَ أَنْ أُشْرِكَ بِكَ وَأَنَا أَعْلَمُ",
    244: "فَإِنَّ الْعَيْنَ حَقٌّ",
    113: "مِنْ غَضَبِهِ وَعِقَابِهِ، وَشَرِّ عِبَادِهِ",
}


import re as _re
def _bare(t):
    t = _re.sub(r"[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]", "", t)
    t = t.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ٱ", "ا").replace("ى", "ي")
    return _re.sub(r"\s+", " ", t)


def dua(did, why):
    x = HISN_BY_ID[did]
    assert _bare(DUA_CHECK[did]) in _bare(x["arabic"]), did
    return {"hisnId": did, "why": why}


header_src = qsrc("65:3")
header = {
    "ref": "65:3",
    "arabic": " ".join(qsrc("65:3")["quran-uthmani"].strip().split(" ")[6:12]),
    "en": "And whoever relies upon Allah - then He is sufficient for him.",
    "ur": "اور جو خدا پر بھروسہ رکھے گا تو وہ اس کو کفایت کرے گا۔",
}
assert header["arabic"] in header_src["quran-uthmani"]
assert header["en"] in header_src["en.sahih"]
assert header["ur"] in header_src["ur.jalandhry"]

E = []

E.append({"id": "angry", "title": "Angry",
  "quran": [
    verse("3:134", "Praises 'those who restrain anger and pardon the people' among the people of Paradise (Ibn Kathir: they do not act on their anger and forgive those who wronged them)."),
    verse("42:37", "'When they are angry, they forgive': Ibn Kathir says forbearance and pardon, not revenge, is their nature when angered."),
    verse("7:199", "Umar was about to act on his anger at Uyaynah; al-Hurr recited this verse and Umar stopped at it (Bukhari 4642; HadeethEnc 3155)."),
    verse("7:200", "Seek refuge from Shaytan's prompting. Ibn Kathir links it to the angry man told to say a'udhu billahi min ash-shaytan (Bukhari 3282)."),
  ],
  "hadith": [
    hadith(4709, "'Do not get angry', repeated as the whole of the Prophet's advice (Bukhari 6116)."),
    hadith(5351, "True strength is controlling yourself when angry (Bukhari 6114, Muslim 2609)."),
    hadith(3578, "The Prophet's prescription for rage: seek refuge with Allah from Shaytan (Bukhari 3282, Muslim 2610)."),
    hadith(3287, "Reward for swallowing anger when able to act on it (Abu Dawud 4777, Tirmidhi 2021). HadeethEnc grades it hasan li-ghayrihi."),
  ],
  "duas": [dua(193, "Hisn chapter 'Invocation for anger': a'udhu billahi min ash-shaytan ir-rajim (Bukhari, Muslim).")]})

E.append({"id": "anxious", "title": "Anxious",
  "quran": [
    verse("13:28", "'By the remembrance of Allah hearts are assured': al-Sa'di calls dhikr the thing that settles a troubled heart."),
    verse("9:51", "Nothing befalls us except what Allah decreed. Belief in the decree takes away fear of what might happen."),
    verse("3:173", "Told an army had gathered against them, the believers said 'hasbunallah wa ni'mal wakil'. Ibrahim said the same in the fire (Bukhari 4563)."),
    verse("2:286", "Allah never burdens a soul beyond its capacity. It came to the Companions when 2:284 had distressed them (Muslim 125)."),
    verse("64:11", "No calamity comes except by Allah's permission, and He guides the believer's heart. Alqamah: the one who knows it is from Allah accepts it (Ibn Kathir)."),
  ],
  "hadith": [
    hadith(66538, "Trusting Allah as He deserves: He provides as He provides for the birds (Tirmidhi 2344)."),
    hadith(66522, "Ibn Abbas's hadith: nobody can harm you except by Allah's decree, and relief comes with distress (Tirmidhi 2516)."),
    hadith(5141, "The words the Prophet used to say at times of distress (Bukhari 6346, Muslim 2730)."),
  ],
  "duas": [
    dua(121, "Hisn 'Invocations in times of worry and grief': refuge from al-hamm (anxiety) and al-hazan (sorrow) (Bukhari 6369)."),
    dua(128, "'Hasbunallahu wa ni'mal wakil', said by Ibrahim and the Companions when afraid (Bukhari 4563). Pairs with 3:173."),
  ]})

E.append({"id": "bored", "title": "Bored",
  "quran": [
    verse("94:7", "When you are free of one task, work hard at the next in worship. Mujahid and others in Ibn Kathir: when done with worldly work, stand for prayer. Directly about free time."),
    verse("94:8", "And direct your longing to your Lord: gives the restless, empty feeling a direction."),
    verse("103:1", "Allah swears by time itself. Surat al-Asr (103:1-3) is the classic text on the value of time."),
    verse("103:2", "All people are in loss..."),
    verse("103:3", "...except those who fill their time with faith, good deeds and advising one another. Ibn Kathir reports al-Shafi'i: had people reflected on this surah alone, it would suffice them."),
    verse("23:115", "'Did you think We created you uselessly?' Answers the feeling that life is empty or pointless."),
  ],
  "hadith": [
    hadith(5449, "Health and free time are two blessings most people waste (Bukhari 6412). Boredom is unused free time."),
    hadith(4704, "Ibn Umar: take from your health for your sickness and from your life for your death, i.e. use time before it is gone (Bukhari 6416)."),
  ],
  "duas": []})

E.append({"id": "confident", "title": "Confident",
  "quran": [
    verse("31:18", "Luqman's advice: do not turn your cheek in contempt or walk exultantly. Confidence without arrogance."),
    verse("17:37", "Do not walk exultantly: 'you will never tear the earth apart or reach the mountains in height' (Ibn Kathir: a rebuke to self-importance)."),
    verse("53:32", "Do not declare yourselves pure. Allah knows best who is God-fearing, which guards against self-satisfaction.",
          ((27, 35),
           "So do not claim yourselves to be pure; He is most knowing of who fears Him.",
           "تو اپنے آپ کو پاک صاف نہ جتاؤ۔ جو پرہیزگار ہے وہ اس سے خوب واقف ہے")),
    verse("18:23", "Never say 'I will surely do it tomorrow'... (read with 18:24)."),
    verse("18:24", "...without 'if Allah wills'. Ibn Kathir: plans and confidence are always tied to Allah's will."),
  ],
  "hadith": [
    hadith(6209, "An atom's weight of arrogance keeps one from Paradise. Looking good is not arrogance: 'arrogance is rejecting the truth and looking down on people' (Muslim 91)."),
    hadith(5512, "Whoever humbles himself for Allah, Allah raises him (Muslim 2588)."),
    hadith(5905, "The man who admired himself in fine clothes and strutted was swallowed by the earth. A warning against self-admiration (Bukhari 5789, Muslim 2088)."),
  ],
  "duas": [
    dua(232, "What to say when praised: 'do not hold me to account for what they say... make me better than they think'. The Hisn source is al-Adab al-Mufrad 761, a Companion's saying (athar)."),
    dua(88, "'Do not leave me to myself even for the blink of an eye': confidence rests on Allah, not the self (Hakim; Albani hasan)."),
  ]})

E.append({"id": "confused", "title": "Confused",
  "quran": [
    verse("29:69", "Those who strive for Us, We will surely guide to Our ways. Guidance follows sincere effort."),
    verse("8:29", "If you fear Allah He will give you a furqan. Ibn Kathir: a criterion to tell truth from falsehood, and a way out."),
    verse("20:114", "'My Lord, increase me in knowledge': Ibn Hajar (Fath al-Bari, Kitab al-Ilm) notes this is the only thing the Prophet was told to ask for more of.",
          ((15, 19),
           "and say, \"My Lord, increase me in knowledge.\"",
           "اور دعا کرو کہ میرے پروردگار مجھے اور زیادہ علم دے")),
    verse("1:6", "'Guide us to the straight path': the request for guidance made in every prayer."),
    verse("2:257", "Allah brings believers out of darknesses into light. Al-Sa'di: the darknesses of ignorance and doubt.",
          ((0, 9),
           "Allah is the ally of those who believe. He brings them out from darknesses into the light.",
           "جو لوگ ایمان لائے ہیں ان کا دوست خدا ہے کہ اُن کو اندھیرے سے نکال کر روشنی میں لے جاتا ہے")),
  ],
  "hadith": [
    hadith(66515, "The lawful and unlawful are clear, and between them are unclear matters. Avoiding them protects your religion (Bukhari 52, Muslim 1599)."),
    hadith(66540, "When unsure what is right: 'ask your heart'. Sin is what unsettles it, even if people tell you otherwise (Muslim 2553; Ahmad)."),
    hadith(66519, "'Leave what makes you doubt for what does not' (Tirmidhi 2518, Nasa'i 5711)."),
  ],
  "duas": [dua(30, "The Prophet's night-prayer opening: 'guide me, by Your leave, to the truth in what they differed over' (Muslim 770).")]})

E.append({"id": "content", "title": "Content",
  "quran": [
    verse("9:59", "'If only they had been satisfied with what Allah and His Messenger gave them.' Its lesson (al-Sa'di): be content with what Allah allots and hope in His bounty."),
    verse("20:131", "Do not stretch your eyes toward others' worldly enjoyment; your Lord's provision is better and lasts longer."),
    verse("16:97", "'We will give him a good life': Ibn Kathir reports from Ibn Abbas and Ali that the good life is contentment (qana'ah)."),
    verse("89:27", "The soul at rest (al-nafs al-mutma'innah)... (read with 89:28)."),
    verse("89:28", "...'well-pleased and pleasing': contentment (rida) in both directions."),
  ],
  "hadith": [
    hadith(3852, "Real wealth is contentment of the soul, not a lot of possessions (Bukhari 6446, Muslim 1051)."),
    hadith(5840, "Safe, healthy and with food for the day: 'as if the whole world were given to him' (Tirmidhi 2346, hasan)."),
    hadith(5341, "Look at those below you, not above, so you do not belittle Allah's blessings on you (Bukhari 6490, Muslim 2963)."),
  ],
  "duas": [dua(87, "'I am pleased with Allah as Lord, Islam as religion and Muhammad as Prophet'. Nearly the same words are sahih in Muslim 386. Note: the Hisn morning/evening reward narration's grading is disputed (Ibn Baz hasan).")]})

E.append({"id": "depressed", "title": "Depressed",
  "quran": [
    verse("93:3", "Revealed when revelation paused and the Prophet was taunted that his Lord had left him (Bukhari 4950): Allah has not abandoned you or become displeased with you."),
    verse("94:5", "With hardship comes ease..."),
    verse("94:6", "...said twice. Ibn Kathir: one hardship will not overcome two eases."),
    verse("12:86", "Ya'qub, in deep grief, takes his sorrow only to Allah: a model for voicing pain."),
    verse("12:87", "Do not despair of Allah's relief (rawh). Only disbelievers give up hope.",
          ((6, 21),
           "and despair not of relief from Allah. Indeed, no one despairs of relief from Allah except the disbelieving people.",
           "اور خدا کی رحمت سے ناامید نہ ہو۔ کہ خدا کی رحمت سے بےایمان لوگ ناامید ہوا کرتے ہیں")),
    verse("3:139", "Revealed after the losses at Uhud (Ibn Kathir): do not weaken and do not grieve."),
  ],
  "hadith": [
    hadith(3701, "No sorrow, sadness or distress befalls a Muslim without Allah expiating sins by it (Bukhari 5641, Muslim 2573)."),
    hadith(3339, "When Allah loves people He tests them, and whoever accepts it has Allah's pleasure (Tirmidhi 2396, Ibn Majah 4031)."),
    hadith(3159, "The believer keeps being tested until he meets Allah free of sin (Tirmidhi 2399, hasan)."),
  ],
  "duas": [
    dua(120, "Ibn Mas'ud's du'a for worry and grief: make the Quran 'the departure of my sorrow' (Ahmad 1/391; Albani sahih)."),
    dua(123, "Hisn 'Invocations for anguish': 'Your mercy I hope for... do not leave me to myself' (Abu Dawud 5090, hasan)."),
    dua(124, "Yunus's du'a from inside the whale (21:87). The Prophet said no Muslim makes it without being answered (Tirmidhi 3505)."),
  ]})

E.append({"id": "doubtful", "title": "Doubtful",
  "quran": [
    verse("2:2", "'This is the Book about which there is no doubt': the anchor for a doubting heart."),
    verse("2:147", "The truth is from your Lord, so never be among the doubters."),
    verse("49:15", "Defines true believers as those who believe and then do not doubt."),
    verse("14:10", "The messengers' answer to doubt: can there be doubt about the Creator of the heavens and earth?",
          ((3, 9),
           "Can there be doubt about Allah, Creator of the heavens and earth?",
           "کیا (تم کو) خدا (کے بارے) میں شک ہے جو آسمانوں اور زمین کا پیدا کرنے والا ہے۔")),
    verse("41:53", "Allah promises to show His signs in the horizons and within ourselves until the truth is clear."),
  ],
  "hadith": [
    hadith(65013, "Shaytan's whisper 'who created your Lord?': seek refuge with Allah and stop (Bukhari 3276, Muslim 134)."),
    hadith(65011, "Being disturbed by such thoughts is 'clear faith' (Muslim 132). Reassures the person who fears their doubts."),
    hadith(65012, "'Praise be to Allah who reduced his plot to whispering' (Abu Dawud 5112). Such whispers are harmless if rejected."),
  ],
  "duas": [
    dua(133, "Hisn 'if you are stricken with doubt in your faith': seek refuge in Allah and stop (Bukhari, Muslim)."),
    dua(134, "Say 'I believe in Allah and His messengers' (Muslim 134)."),
    dua(135, "Recite 57:3 'He is the First and the Last...' (Abu Dawud 5110; Albani hasan al-isnad, a saying of Ibn Abbas)."),
  ]})

E.append({"id": "grateful", "title": "Grateful",
  "quran": [
    verse("14:7", "'If you are grateful, I will surely increase you': the central promise about gratitude."),
    verse("2:152", "Remember Me and I will remember you; be grateful to Me."),
    verse("16:18", "You could never count Allah's favours."),
    verse("31:12", "Luqman's wisdom: be grateful, and gratitude benefits the one who is grateful."),
    verse("27:19", "Sulayman's du'a to be enabled to give thanks for Allah's favours on him and his parents.",
          ((5, 24),
           "My Lord, enable me to be grateful for Your favor which You have bestowed upon me and upon my parents and to do righteousness of which You approve. And admit me by Your mercy into [the ranks of] Your righteous servants.",
           "اے پروردگار! مجھے توفیق عطا فرما کہ جو احسان تونے مجھ پر اور میرے ماں باپ پر کئے ہیں ان کا شکر کروں اور ایسے نیک کام کروں کہ تو ان سے خوش ہوجائے اور مجھے اپنی رحمت سے اپنے نیک بندوں میں داخل فرما")),
  ],
  "hadith": [
    hadith(4830, "'Should I not be a grateful servant?' Gratitude is shown in worship (Bukhari 4837, Muslim 2820)."),
    hadith(5798, "Allah is pleased with the one who praises Him for a bite of food or a drink (Muslim 2734)."),
    hadith(66255, "Whoever does not thank people does not thank Allah (Abu Dawud 4811, Tirmidhi 1954)."),
  ],
  "duas": [dua(59, "'Help me to remember You, to thank You and to worship You well', which the Prophet told Mu'adh to say after every prayer (Abu Dawud 1522).")]})

E.append({"id": "greedy", "title": "Greedy",
  "quran": [
    verse("102:1", "'Competition in increase diverts you'..."),
    verse("102:2", "...'until you visit the graves'. Ibn Kathir cites under this surah the hadith 'the son of Adam says: my wealth, my wealth' (Muslim 2958)."),
    verse("64:16", "'Whoever is protected from the stinginess (shuhh) of his soul, those are the successful.' Shuhh is greed together with withholding."),
    verse("18:46", "Wealth and children are adornment; lasting good deeds are better with Allah."),
    verse("3:180", "Those who greedily hold back Allah's bounty: it becomes a collar on the Day of Resurrection (Bukhari 1403 explains it)."),
  ],
  "hadith": [
    hadith(4963, "With two valleys of wealth he would want a third; only dust fills his belly (Bukhari 6439, Muslim 1048).",
           note="HadeethEnc's Urdu follows a different authentic wording (one valley of gold, then wanting two; narrators Ibn Abbas, Anas, Ibn al-Zubayr, Abu Musa). Its Arabic is given in arabicUr. Same meaning."),
    hadith(3703, "Wealth taken with greed (ishraf al-nafs) is not blessed, 'like one who eats and is never full' (Bukhari 1472, Muslim 1035)."),
    hadith(5787, "Beware of shuhh (greedy stinginess): it destroyed those before you (Muslim 2578)."),
  ],
  "duas": [
    dua(136, "'Make the lawful enough for me instead of the unlawful, and free me by Your bounty from needing anyone else' (Tirmidhi 3563, hasan)."),
    dua(60, "Refuge from miserliness (bukhl) and the fitnah of this world (Bukhari)."),
  ]})

E.append({"id": "guilty", "title": "Guilty",
  "quran": [
    verse("39:53", "'Do not despair of the mercy of Allah; He forgives all sins.' Ibn Kathir: a call to every sinner to repent."),
    verse("3:135", "Those who remember Allah after a sin, seek forgiveness and do not persist in it."),
    verse("4:110", "Whoever does wrong and then seeks forgiveness will find Allah Forgiving and Merciful."),
    verse("25:70", "For those who repent, Allah replaces their bad deeds with good ones."),
    verse("11:114", "Revealed about a man who came to the Prophet remorseful after kissing a woman: 'good deeds do away with misdeeds' (Bukhari 526, Muslim 2763)."),
  ],
  "hadith": [
    hadith(4313, "Allah is happier with His servant's repentance than a man who finds his lost camel in the desert (Bukhari 6309, Muslim 2747)."),
    hadith(4318, "Allah stretches out His hand by night and day to accept repentance (Muslim 2759)."),
    hadith(5456, "'Were your sins to reach the clouds of the sky...' (Tirmidhi 3540, hasan)."),
    hadith(5344, "Every son of Adam sins, and the best sinners are those who repent (Tirmidhi 2499, hasan)."),
  ],
  "duas": [
    dua(79, "Sayyid al-istighfar, the master supplication for forgiveness (Bukhari 6306)."),
    dua(57, "'I have wronged myself greatly and none forgives sins but You' (Bukhari, Muslim)."),
    dua(140, "Hisn 'What to say and do if you commit a sin': ablution, two rak'ahs, then seek forgiveness (Abu Dawud 1521, Tirmidhi 406)."),
  ]})

E.append({"id": "happy", "title": "Happy",
  "quran": [
    verse("10:58", "Rejoice in Allah's bounty and mercy, which is better than what they gather. It names the right thing to be happy about."),
    verse("93:11", "Speak of your Lord's favour: happiness expressed as gratitude."),
    verse("27:40", "Sulayman, on receiving a blessing: this is from my Lord's favour, to test whether I am grateful.",
          ((20, 41),
           "This is from the favor of my Lord to test me whether I will be grateful or ungrateful. And whoever is grateful - his gratitude is only for [the benefit of] himself. And whoever is ungrateful - then indeed, my Lord is Free of need and Generous.",
           "یہ میرے پروردگار کا فضل ہے تاکہ مجھے آزمائے کہ میں شکر کرتا ہوں یا کفران نعمت کرتا ہوں اور جو شکر کرتا ہے تو اپنے ہی فائدے کے لئے شکر کرتا ہے اور جو ناشکری کرتا ہے تو میرا پروردگار بےپروا (اور) کرم کرنے والا ہے")),
    verse("57:23", "Do not exult over what He gave you. Ibn Kathir quotes Ikrimah: make your joy gratitude and your grief patience."),
  ],
  "hadith": [
    hadith(3298, "'If something good happens to him he is grateful' (Muslim 2999)."),
    hadith(11244, "On receiving good news the Prophet fell into prostration of thanks (Abu Dawud 2774, Tirmidhi 1578)."),
    hadith(8900, "Being praised for a good deed is 'instant good news for the believer' (Muslim 2642). Joy at a good deed is sound."),
  ],
  "duas": [dua(218, "What the Prophet said when something pleased him: 'Praise be to Allah by whose favour good things are completed' (Ibn Majah 3803; Albani hasan).")]})

E.append({"id": "hurt", "title": "Hurt",
  "quran": [
    verse("15:97", "Allah knows your chest is tightened by what people say..."),
    verse("15:98", "...the cure: glorify Him and be among those who prostrate (Ibn Kathir)."),
    verse("16:127", "Be patient, and do not grieve over them or be distressed by their scheming."),
    verse("41:34", "Repel harm with what is better. Ibn Abbas (in Ibn Kathir): patience when angered, forbearance and pardon when wronged."),
    verse("42:43", "Whoever is patient and forgives, that is a matter of real resolve."),
    verse("24:22", "Revealed about Abu Bakr, hurt by his relative Mistah's part in the slander, who then resumed supporting him: 'would you not like Allah to forgive you?' (Bukhari 4750)."),
  ],
  "hadith": [
    hadith(3743, "Accused of unfairness, the Prophet was visibly hurt and said Musa was hurt more than this and was patient (Bukhari 3150, Muslim 1062)."),
    hadith(3863, "A man whose relatives repay his kindness with harm: Allah remains his supporter as long as he keeps it up (Muslim 2558)."),
    hadith(5492, "The believer who mixes with people and bears their harm patiently is better (Tirmidhi 2507, Ibn Majah 4032)."),
  ],
  "duas": [dua(144, "Hisn 'when something you dislike happens': 'qaddarallahu wa ma sha'a fa'al' instead of dwelling on 'if only' (Muslim 2664). A general fit, not specific to being hurt by people.")]})

E.append({"id": "indecisive", "title": "Indecisive",
  "quran": [
    verse("3:159", "Once you have decided, rely on Allah: consult, decide, then trust Him.",
          ((24, 34),
           "And when you have decided, then rely upon Allah. Indeed, Allah loves those who rely [upon Him].",
           "اور جب (کسی کام کا) عزم مصمم کرلو تو خدا پر بھروسا رکھو۔ بےشک خدا بھروسا رکھنے والوں کو دوست رکھتا ہے")),
    verse("2:216", "You may dislike what is good for you; Allah knows and you do not. The reason istikhara leaves the choice to Allah.",
          ((7, 28),
           "But perhaps you hate a thing and it is good for you; and perhaps you love a thing and it is bad for you. And Allah Knows, while you know not.",
           "مگر عجب نہیں کہ ایک چیز تم کو بری لگے اور وہ تمہارے حق میں بھلی ہو اور عجب نہیں کہ ایک چیز تم کو بھلی لگے اور وہ تمہارے لئے مضر ہو۔ اور ان باتوں کو) خدا ہی بہتر جانتا ہے اور تم نہیں جانتے")),
    verse("42:38", "Believers settle their affairs by mutual consultation: when unsure, seek counsel."),
    verse("12:67", "Ya'qub takes precautions, then says the decision is Allah's alone and relies on Him: take the means, then trust."),
  ],
  "hadith": [
    hadith(3293, "The istikhara prayer, taught 'for all matters' (Bukhari 1162).",
           note="HadeethEnc's Urdu follows the Arabic in arabicUr, which adds 'kulliha' ('all' matters); same du'a."),
    hadith(5493, "Pursue what benefits you, seek Allah's help, do not feel helpless, and do not torment yourself with 'if only' afterwards (Muslim 2664)."),
  ],
  "duas": [
    dua(74, "The istikhara du'a (Bukhari)."),
    dua(139, "'There is no ease except what You make easy', for when a matter feels hard (Ibn Hibban 2427)."),
  ]})

E.append({"id": "hypocritical", "title": "Hypocritical",
  "quran": [
    verse("61:2", "'Why do you say what you do not do?'..."),
    verse("61:3", "...Allah hates it greatly. The Quran's direct address on words not matching deeds."),
    verse("2:44", "Enjoining good on others while forgetting yourself."),
    verse("4:142", "Hypocrites pray lazily and to be seen: the marks of hypocrisy and showing off (riya')."),
    verse("4:146", "The door stays open: those who repent, reform and make their religion sincere for Allah are with the believers."),
  ],
  "hadith": [
    hadith(5846, "Hanzalah feared he was a hypocrite because his faith dipped at home. The Prophet reassured him: 'an hour and an hour' (Muslim 2750)."),
    hadith(3381, "Riya' (showing off) is the minor shirk the Prophet feared most for us (Ahmad; HadeethEnc hasan)."),
    hadith(66537, "The four traits of hypocrisy, so one can check and give them up (Bukhari 34, Muslim 58)."),
  ],
  "duas": [dua(203, "Refuge from shirk knowingly and forgiveness for what we do unknowingly. The Prophet taught it after warning of hidden shirk (Ahmad 4/403).")]})

E.append({"id": "jealous", "title": "Jealous",
  "quran": [
    verse("4:32", "Do not wish for what Allah gave others over you; ask Allah of His bounty (Ibn Kathir: forbids envious wishing)."),
    verse("4:54", "Rebukes envying people for what Allah gave them of His bounty.",
          ((0, 9),
           "Or do they envy people for what Allah has given them of His bounty?",
           "یا جو خدا نے لوگوں کو اپنے فضل سے دے رکھا ہے اس کا حسد کرتے ہیں")),
    verse("43:32", "Allah Himself shares out livelihood and rank, so envy is objecting to His distribution."),
    verse("59:10", "The du'a 'put no rancour in our hearts toward those who believe'."),
  ],
  "hadith": [
    hadith(4706, "'Do not envy one another... be brothers' (Muslim 2564)."),
    hadith(3772, "The only allowed 'envy' is wishing for the good another has in knowledge or generous spending (Bukhari 73, Muslim 816)."),
    hadith(66520, "Love for your brother what you love for yourself, the opposite of envy (Bukhari 13, Muslim 45)."),
  ],
  "duas": [dua(244, "When something of another's impresses you, ask Allah to bless it for them rather than envy it (Ahmad, Ibn Majah 3509).")]})

E.append({"id": "lazy", "title": "Lazy",
  "quran": [
    verse("53:39", "Man has nothing but what he strives for..."),
    verse("53:40", "...and his effort will be seen."),
    verse("3:133", "'Hasten to forgiveness from your Lord': a command to hurry, not delay."),
    verse("57:21", "'Race toward forgiveness': a command to compete in good."),
    verse("9:105", "'Do (your deeds)': Allah, His Messenger and the believers will see your work."),
  ],
  "hadith": [
    hadith(3731, "Shaytan's three knots keep you asleep. Dhikr, wudu and prayer untie them, otherwise you wake 'low-spirited and sluggish' (Bukhari 1142, Muslim 776)."),
    hadith(5914, "The Prophet sought refuge from incapacity and laziness (Bukhari 2823, Muslim 2706)."),
    hadith(3138, "'Hasten to good deeds' before trials come, against putting things off (Muslim 118)."),
  ],
  "duas": [dua(121, "Seeks refuge from al-'ajz wal-kasal (weakness and laziness) (Bukhari 6369). Also listed under Anxious.")]})

E.append({"id": "lonely", "title": "Lonely",
  "quran": [
    verse("2:186", "'I am near; I answer the call of the caller': Allah's closeness to whoever calls Him."),
    verse("50:16", "We are closer to him than his jugular vein, and know what his soul whispers."),
    verse("57:4", "He is with you wherever you are (Ibn Kathir: with His knowledge and watchfulness).",
          ((29, 39),
           "and He is with you wherever you are. And Allah, of what you do, is Seeing.",
           "اور تم جہاں کہیں ہو وہ تمہارے ساتھ ہے۔ اور جو کچھ تم کرتے ہو خدا اس کو دیکھ رہا ہے")),
    verse("20:46", "To Musa and Harun facing Pharaoh alone: 'I am with you both; I hear and I see'."),
    verse("9:40", "Two alone in the cave, hunted: 'Do not grieve; Allah is with us'.",
          ((11, 23),
           "when they were in the cave and he said to his companion, \"Do not grieve; indeed Allah is with us.\"",
           "جب وہ دونوں غار (ثور) میں تھے اس وقت پیغمبر اپنے رفیق کو تسلی دیتے تھے کہ غم نہ کرو خدا ہمارے ساتھ ہے")),
  ],
  "hadith": [
    hadith(6207, "'The One you call is not deaf or absent. He is with you, All-Hearing, Near' (Bukhari 2992, Muslim 2704)."),
    hadith(3636, "'I am with him when he remembers Me... if he comes walking, I come running' (Bukhari 7405, Muslim 2675)."),
    hadith(3447, "'What do you think of two when Allah is the third?' Said in the cave, where 9:40 took place (Bukhari 3653, Muslim 2381).",
           note="HadeethEnc's Urdu follows slightly different Arabic wording (in arabicUr); same meaning."),
  ],
  "duas": [dua(113, "Hisn chapter 'afraid to go to sleep or feel lonely and depressed' (Abu Dawud 3893, Tirmidhi 3528; Albani hasan). The hadith itself is about fear at night.")]})

for e in E:
    for v in e["quran"]:
        if v["part"]:
            v["partNote"] = "Only the words: " + v["en"]

out = {"header": header, "emotions": E}
assert len(E) == 18
path = os.path.join(BASE, "emotions.json")
json.dump(out, open(path, "w"), ensure_ascii=False, indent=2)
for e in E:
    print(e["id"], "Q", len(e["quran"]), "H", len(e["hadith"]), "D", len(e["duas"]))
print("written", path)
