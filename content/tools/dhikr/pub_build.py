import json, sys, re
sys.path.insert(0, '.')
import lk

DHIKR = '/Users/vyro/Projects/IOS Projects/Muttaqi/Muttaqi/Resources/Data/Dhikr.json'
OUT = 'dhikr-published.json'

def he_rec(i, l):
    d = json.load(open(f'he/{l}/{i}.json'))
    return d
EN_ATTR = {}
def he(i, l):
    d = he_rec(i, l)
    attr = he_rec(i, 'en').get('attribution', '').strip()
    return (d['hadeeth'], f'HadeethEnc.com (Encyclopedia of Translated Prophetic Hadiths) #{i} — {attr}',
            f'https://hadeethenc.com/api/v1/hadeeths/one/?language={l}&id={i}', False)

HISN = {}
def hisn(ch, item):
    if ch not in HISN:
        fn = f'hisn/en_{ch}.json' if ch != 27 else 'hisn/en_27_live.json'
        d = json.loads(open(fn, encoding='utf-8-sig').read())
        HISN[ch] = {it['ID']: it for it in list(d.values())[0]}
    it = HISN[ch][item]
    return (it['TRANSLATED_TEXT'], f"Hisn al-Muslim (Sa'id ibn Wahf al-Qahtani), English edition on hisnmuslim.com — chapter {ch}, item {item}",
            f'http://www.hisnmuslim.com/api/en/{ch}.json', False)

TRANSLATOR = {('eng', 'bukhari'): 'English: Muhammad Muhsin Khan', ('eng', 'muslim'): 'English: Abdul Hamid Siddiqui'}
NAMES = {'bukhari': 'Sahih al-Bukhari', 'muslim': 'Sahih Muslim', 'tirmidhi': "Jami' at-Tirmidhi", 'abudawud': 'Sunan Abi Dawud',
         'nasai': "Sunan an-Nasa'i", 'ibnmajah': 'Sunan Ibn Majah'}
def ha(c, n, l, sub=None):
    hs = lk.load(l, c).get(str(n), [])
    if sub is not None:
        hs = [h for h in hs if str(h.get('arabicnumber')) == sub]
    h = hs[0]
    who = TRANSLATOR.get((l, c), ('English' if l == 'eng' else 'Urdu') + ': translator not named in hadith-api metadata')
    return (h['text'], f"{NAMES[c]} {sub or n} — {who} (via fawazahmed0/hadith-api {l}-{c}; LICENSING RISK: text from a commercially published translation)",
            f'https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/{l}-{c}/{h["hadithnumber"]}.json', True)

QED = {'en': ('en.sahih', 'Saheeh International'), 'ur': ('ur.jalandhry', 'Fateh Muhammad Jalandhry'), 'ur-alt': ('ur.junagarhi', 'Muhammad Junagarhi')}
def qr(s, vs, l):
    vs = vs if isinstance(vs, list) else [vs]
    ed, name = QED[l]
    texts = []
    for v in vs:
        d = json.load(open(f'qc/{s}-{v}.json'))
        texts.append([e for e in d['data'] if e['edition']['identifier'] == ed][0]['text'])
    ref = f'{s}:{vs[0]}' + (f'-{vs[-1]}' if len(vs) > 1 else '')
    return (' '.join(texts), f'Quran {ref} — {name} ({ed})', f'https://api.alquran.cloud/v1/ayah/{s}:{vs[0]}/{ed}', False)

import unicodedata
IGN = re.compile(r'[\u064B-\u065F\u0670\u0640\u200c-\u200f\u061c]')
VAR = str.maketrans({'\u064A': '\u06CC', '\u0649': '\u06CC', '\u0643': '\u06A9', '\u0647': '\u06C1', '\u06BE': '\u06C1', '\u0629': '\u06C1', '\u06D2': '\u06CC'})
def _norm_map(t):
    out, idx = [], []
    for i, ch0 in enumerate(t):
        for ch in unicodedata.normalize('NFD', ch0):
            if IGN.match(ch) or unicodedata.category(ch) == 'Mn':
                continue
            if ch.isspace():
                if out and out[-1] == ' ':
                    continue
                ch = ' '
            out.append(ch.translate(VAR)); idx.append(i)
    return ''.join(out), idx
def locate(full, text):
    if text in full:
        return text
    nf, idx = _norm_map(full); nt, _ = _norm_map(text)
    nt = nt.strip()
    k = nf.find(nt)
    if k < 0:
        return None
    a = idx[k]; b = idx[k + len(nt) - 1] + 1
    while b < len(full) and IGN.match(full[b]):
        b += 1
    return full[a:b]
def cut(src, text):
    full, source, url, risk = src
    found = locate(full, text)
    if found is None:
        raise AssertionError(f'NOT VERBATIM in {source}:\n  {text}\n  --- source: {full[:400]}')
    text = found
    assert text in full
    o = {'text': text, 'source': source, 'sourceUrl': url}
    if risk: o['licensingRisk'] = True
    if source.startswith('HadeethEnc') and text != full:
        o['fullText'] = full
    return o

def whole(src):
    return cut(src, src[0])

S_SUB = 'سُبْحَانَ اللَّهِ'; S_HAM = 'الْحَمْدُ لِلَّهِ'; S_AKB = 'اللَّهُ أَكْبَرُ'; S_TAH = 'لَا إِلَهَ إِلَّا اللَّهُ'
S_TAHLIL100 = 'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ'
def STEP(a):
    return {
        S_SUB: {'en': cut(he(6076, 'en'), 'glory be to Allah'), 'ur': cut(he(5475, 'ur'), 'اللہ پاک ہے')},
        S_HAM: {'en': cut(he(6076, 'en'), 'praise be to Allah'), 'ur': cut(he(5475, 'ur'), 'ساری تعریف اللہ کی ہے')},
        S_AKB: {'en': cut(he(6076, 'en'), 'Allah is the Most Great'), 'ur': cut(he(5475, 'ur'), 'اللہ سب سے بڑا ہے')},
        S_TAH: {'en': cut(he(3567, 'en'), 'there is no god but Allah'), 'ur': cut(he(5475, 'ur'), 'اللہ کے علاوہ کوئی معبود برحق نہيں ہے')},
        S_TAHLIL100: {'en': cut(he(10948, 'en'), 'There is no god except Allah. He is One and has no partner with Him. To Him belongs sovereignty and to Him belongs praise, and He is Omnipotent over everything'),
                      'ur': cut(he(10948, 'ur'), 'اللہ کے سوا کوئی معبود بر حق نہيں ہے، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہت ہے، اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے')},
        'لَا إِلَهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ': {'en': cut(he(6273, 'en'), 'there is no god but Allah, and Allah is Greatest'), 'ur': cut(he(6273, 'ur'), 'اللہ کے سوا کوئی معبود برحق نہیں ہے، اللہ سب سے بڑا ہے')},
        'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ': {'en': cut(he(6273, 'en'), 'there is no god but Allah alone'), 'ur': cut(he(6273, 'ur'), 'اللہ واحد کے سوا کوئی معبود برحق نہیں ہے')},
        'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ': {'en': cut(he(6273, 'en'), 'there is no god but Allah alone; He has no partner'), 'ur': cut(he(6273, 'ur'), 'اللہ واحد کے سوا کوئی معبود نہیں ہے اس کا کوئی شریک و ساجھی نہیں')},
        'لَا إِلَهَ إِلَّا اللَّهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ': {'en': cut(he(6273, 'en'), 'There is no god but Allah. To Him is the dominion and to Him is praise'), 'ur': cut(he(6273, 'ur'), 'اللہ کے سوا کوئی معبود برحق نہیں، اسی کے لیے بادشاہت ہے اور اسی کے لیے حمد ہے')},
        'لَا إِلَهَ إِلَّا اللَّهُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ': {'en': cut(he(6273, 'en'), 'there is no god but Allah, and there is no might nor power save with Allah'), 'ur': cut(he(6273, 'ur'), 'اللہ کے سوا کوئی معبود برحق نہیں ہے، اور گناہ سے بچنے اور بھلے کام کرنے کی طاقت نہیں ہے، مگر اللہ تعالیٰ کی توفیق سے')},
    }[a]

def P(en=None, ur=None): return {'en': en, 'ur': ur}

E = {}
N = {}
ALT = {}

E['subhanallahi-wa-bihamdihi'] = dict(
    phrase=P(cut(he(5516, 'en'), 'glory be to Allah, and praise be to Him'), cut(he(5402, 'ur'), 'اللہ پاک ہے اپنی تعریفوں کے ساتھ')),
    hadith=P(cut(he(5516, 'en'), 'Whoever says ‘subhān Allah wa bihamdih (glory be to Allah, and praise be to Him)’ one hundred times a day, his sins will be erased, even if they were as much as the foam of the sea.'),
             cut(he(5516, 'ur'), 'جس نے دن میں سو بار ”سُبْحَانَ اللَّهِ وَبِحَمْدِهِ“ پڑھا، اس کے گناہ معاف کر دیے جاتے ہیں، خواہ سمندر کی جھاگ کے برابر ہی کیوں نہ ہوں۔')))
N['subhanallahi-wa-bihamdihi'] = ['phrase.ur is cut from HadeethEnc #5402 (the same words in Muslim 2731); #5516 Urdu leaves the phrase in Arabic.']

E['subhanallahi-wa-bihamdihi-subhanallahil-azim'] = dict(
    phrase=P(cut(hisn(130, 256), 'How perfect Allah is and I praise Him. How perfect Allah is, The Supreme.'),
             cut(ha('muslim', 2694, 'urd'), 'اللہ ہر اس چیز سے پاک ہے جو اس کے شایان شان نہیں اور سب حمد اسی کو سزاوار ہے ، اللہ ہر اس چیز سے پاک ہے جو اس کے شایان شان نہیں اور عظمت کی ہر صفت سے متصف ہے')),
    hadith=P(cut(he(5507, 'en'), 'There are two words that are light on the tongue, heavy on the scale, and dear to the Most Merciful'),
             cut(he(5507, 'ur'), 'دو کلمے ایسے ہیں جو زبان پر بڑے ہلکے ہیں، میزان میں بڑے وزنی ہیں، رحمٰن کو بڑے محبوب ہیں۔')))
N['subhanallahi-wa-bihamdihi-subhanallahil-azim'] = ['HadeethEnc #5507 gives the two phrases in the reverse order (Bukhari 6406 wording), so phrase.en is from Hisn al-Muslim (same order as the app). HadeethEnc Urdu leaves the phrase in Arabic, so phrase.ur is from the hadith-api Urdu of Muslim 2694.']

E['subhanallah'] = dict(
    phrase=P(cut(he(4558, 'en'), 'Glory be to Allah'), cut(he(5475, 'ur'), 'اللہ پاک ہے')),
    hadith=P(cut(he(3762, 'en'), '"Is anyone of you unable to earn a thousand good deeds every day?" One of those present asked: How can one earn a thousand good deeds? He replied: "He glorifies Allah a hundred times, a thousand good deeds will be recorded for him, or a thousand sins will be erased from his record."'),
             cut(he(3762, 'ur'), 'کیا تم میں سے کوئی شخص اس بات سے عاجز ہے کہ ہر دن ایک ہزار نیکیاں کما لے؟۔ آپ ﷺ کے ساتھ بیٹھے لوگوں میں سے ایک شخص نے پوچھا: آدمی ہزار نیکیاں کیسے کما سکتا ہے؟۔ آپ ﷺ نے فرمایا: وہ سو دفعہ سبحان اللہ کہے، اس کے لیے ایک ہزار نیکیاں لکھ دی جاتی ہیں یا اس کے ایک ہزار گناہ معاف کر دیے جاتے ہیں۔')))

E['subhanallahil-azimi-wa-bihamdihi'] = dict(
    phrase=P(cut(he(4201, 'en'), 'Glory be to Allah the Most Great and praise is due to Him'), None),
    hadith=P(cut(he(4201, 'en'), 'Whoever says: Subhānallāh al-‘azhīm wa bihamdih (Glory be to Allah the Most Great and praise is due to Him), a date palm will be planted for him in Paradise.'),
             cut(he(4201, 'ur'), 'اس کے لیے جنت میں کھجور کا ایک درخت لگایا جائے گا')))
N['subhanallahil-azimi-wa-bihamdihi'] = ['phrase.ur null: HadeethEnc #4201 Urdu quotes the phrase in Arabic (and without "al-\'Azim"), and the hadith-api Urdu of Tirmidhi 3464 also leaves it untranslated. hadith.ur is therefore cut to the virtue clause only.']

E['subhanallahi-wa-bihamdihi-adada-khalqihi-wa'] = dict(
    phrase=P(cut(he(5508, 'en'), 'Glory be to Allah and all praise is due to Him by the number of His creation, by His own pleasure, by the weight of His Throne, and by the ink of His words.'),
             cut(he(5508, 'ur'), 'اللہ کی حمد و تسبیح بیان کرتا ہوں اس کی مخلوق کی تعداد اور اس کی رضا کے بقدر نیز اس کے عرش کے وزن اور اس کے کلمات کی روشنائی کے برابر')),
    hadith=P(cut(he(5508, 'en'), 'I said four words three times after I had left you; if they were to be weighed against what you have been saying since morning, they would outweigh them'),
             cut(he(5508, 'ur'), 'میں نے تمہارے پاس سے جانے کے بعد چار ایسے کلمات تین بار کہے ہیں کہ جو کچھ تم نے صبح سے اب تک پڑھا ہے اگر اس کا ان کلمات کے ساتھ وزن کرو تو ان کلمات کا وزن زیادہ ہو گا')))

E['subhanallahi-walhamdulillah'] = dict(
    phrase=P(cut(he(65004, 'en'), 'glory and praise be to Allah'), cut(he(6211, 'ur'), 'اللہ پاک ہے، تمام تعریفیں اللہ کی ہیں')),
    hadith=P(cut(he(65004, 'en'), 'subhān Allah wa al-hamdulillāh (glory and praise be to Allah) fills what is between the heavens and the earth'),
             cut(he(65004, 'ur'), 'سبحان الله اور الحمد لله آسمانوں سے زمین تک کی وسعت کو بھر دیتے ہیں')))
N['subhanallahi-walhamdulillah'] = ['phrase.ur is the first two clauses of HadeethEnc #6211 Urdu (Muslim 2695), because #65004 Urdu keeps the words in Arabic.']

E['subhanallahi-adada-ma-khalaq-wa-subhanallahi'] = dict(phrase=P(None, None), hadith=P(None, None))
N['subhanallahi-adada-ma-khalaq-wa-subhanallahi'] = ['No published English/Urdu found in the allowed sources: the hadith (Musnad Ahmad 22144, an-Nasa\'i al-Kubra 9921) is not in HadeethEnc, Hisn al-Muslim or hadith-api (no Musnad Ahmad). Consider removing the entry or sourcing a published Musnad Ahmad translation.']

E['subbuhun-quddusun-rabbul-malaikati-war-ruh'] = dict(
    phrase=P(cut(he(6015, 'en'), 'You are the Most Glorious, the Most Holy, and the lord of the angels and of the Spirit.'),
             cut(he(6015, 'ur'), 'نہايت ہی پاک، بڑا مقدس ہے فرشتوں اور جيريل عليہ السلام کا رب')),
    hadith=P(cut(he(6015, 'en'), 'the Messenger of Allah (may Allah’s peace and blessings be upon him) used to recite in his bowing and prostration: "You are the Most Glorious, the Most Holy, and the lord of the angels and of the Spirit."'),
             cut(he(6015, 'ur'), 'بے شک رسول اللہ ﷺ اپنے رکوع وسجود میں ”سُبُّوحٌ قُدُّوسٌ رَبُّ الْمَلَائِکَةِ وَالرُّوحِ“ (نہايت ہی پاک، بڑا مقدس ہے فرشتوں اور جيريل عليہ السلام کا رب) پڑھا کرتے تھے۔')))

E['subhanal-malikil-quddus'] = dict(
    phrase=P(cut(hisn(33, 119), 'How perfect The King, The Holy One is.'), None),
    hadith=P(cut(hisn(33, 119), 'How perfect The King, The Holy One is.(three times) on the third time he would raise his voice, elongate it and add: (Lord of the angles and the Rooh (i.e. Jibra-eel).)'),
             cut(ha('nasai', 1732, 'urd'), 'اور جب سلام پھیرتے تھے، تو «سبحان الملك القدوس» تین بار کہتے، اور تیسری بار اپنی آواز بلند کرتے۔')))
N['subhanal-malikil-quddus'] = ['Not in HadeethEnc. phrase.ur null: the only Urdu found (hadith-api Nasa\'i 1732 / Abu Dawud 1430) leaves the words in Arabic. The Hisn English contains the source typo "angles" (for angels).']

E['alhamdulillah'] = dict(
    phrase=P(cut(he(65004, 'en'), 'praise be to Allah'), cut(he(5475, 'ur'), 'ساری تعریف اللہ کی ہے')),
    hadith=P(cut(he(65004, 'en'), 'al-hamdulillāh (praise be to Allah) fills the Scale'), cut(he(65004, 'ur'), 'الحمد لله ترازو کو بھر دیتا ہے')))

E['rabbana-wa-lakal-hamd-hamdan-kathiran'] = dict(
    phrase=P(cut(hisn(18, 39), 'Our Lord, for You is all praise, an abundant beautiful blessed praise.'), None),
    hadith=P(cut(ha('bukhari', 799, 'eng'), 'A man behind him said, "Rabbana wa laka l-hamdu, hamdan kathiran taiyiban mubarakan fihi" (O our Lord! All the praises are for You, many good and blessed praises). When the Prophet completed the prayer, he asked, "Who has said these words?" The man replied, "I." The Prophet said, "I saw over thirty angels competing to write it first."'),
             cut(ha('bukhari', 799, 'urd'), 'اس پر آپ صلی اللہ علیہ وسلم نے فرمایا کہ میں نے تیس سے زیادہ فرشتوں کو دیکھا کہ ان کلمات کو لکھنے میں وہ ایک دوسرے پر سبقت لے جانا چاہتے تھے۔')))
N['rabbana-wa-lakal-hamd-hamdan-kathiran'] = ['Not in HadeethEnc; hadith (Bukhari 799) only in hadith-api. phrase.ur null: the hadith-api Urdu leaves the words in Arabic.']

E['alhamdulillahi-ala-kulli-hal'] = dict(
    phrase=P(cut(hisn(106, 218), 'Praise is to Allah in all circumstances.'), cut(ha('ibnmajah', 3803, 'urd'), 'ہر حال میں اللہ کا شکر ہے')),
    hadith=P(cut(hisn(106, 218), "And if something happened that displeased him, he used to say:((Alhamdulillaahi 'alaa kulli haal)).Praise is to Allah in all circumstances."),
             cut(ha('ibnmajah', 3803, 'urd'), 'اور جب کوئی ناپسندیدہ بات دیکھتے تو فرماتے: «الحمد لله على كل حال» ہر حال میں اللہ کا شکر ہے ۔')))

E['la-ilaha-illallah'] = dict(
    phrase=P(cut(he(3567, 'en'), 'there is no god but Allah'), cut(he(5475, 'ur'), 'اللہ کے علاوہ کوئی معبود برحق نہيں ہے')),
    hadith=P(cut(he(3567, 'en'), 'The best dhikr is lā ilāha illa Allah (there is no god but Allah), and the best supplication is Alhamdulillah (praise be to Allah).'),
             cut(he(3567, 'ur'), 'سب سے افضل ذکر "لا الہ الا اللہ" اور سب سے افضل دعا "الحمد للہ" ہے۔')))

E['la-ilaha-illallahu-wahdahu-la-sharika'] = dict(
    phrase=P(cut(he(65905, 'en'), 'There is no god but Allah. He is One, and He has no partner with Him; to Him belong the sovereignty and praise, and He is competent over all things'),
             cut(he(5517, 'ur'), 'اللہ کے سوا کوئی حقیقی معبود نہيں ہے، وہ اکیلا ہے، اس کا کوئی شریک نہيں ہے، اسی کی بادشاہت ہے، اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے')),
    hadith=P(cut(he(65905, 'en'), 'he will have a reward equivalent to that of emancipating ten slaves; a hundred good deeds will be added to his record; a hundred of his sins will be erased; and he will be shielded against the devil on that day till the evening; and no one will exceed him in doing good deeds except someone who says more than that.'),
             cut(ha('bukhari', 6403, 'urd'), 'دن میں سو دفعہ پڑھا اسے دس غلاموں کو آزاد کرنے کا ثواب ملے گا اور اس کے لیے سو نیکیاں لکھ دی جائیں گی اور اس کی سو برائیاں مٹا دی جائیں گی اور اس دن وہ شیطان کے شر سے محفوظ رہے گا شام تک کے لیے اور کوئی شخص اس دن اس سے بہتر کام کرنے والا نہیں سمجھا جائے گا، سوا اس کے جو اس سے زیادہ کرے۔')))
N['la-ilaha-illallahu-wahdahu-la-sharika'] = ['HadeethEnc #65905 (the 100-times hadith) has no Urdu; hadith.ur is the hadith-api Urdu of Bukhari 6403. phrase.ur is from HadeethEnc #5517 (same words, 10-times hadith).',
    'Optional extra for the 10-times virtue: HadeethEnc #5517 has both en and ur.']

E['allahu-akbar'] = dict(
    phrase=P(cut(he(4558, 'en'), 'Allah is the Greatest'), cut(he(5475, 'ur'), 'اللہ سب سے بڑا ہے')),
    hadith=P(cut(he(4558, 'en'), 'Every Tasbīh (saying: Glory be to Allah) is charity; every Takbīr (saying: Allah is the Greatest) is charity; every Tahmīd (saying: Praise be to Allah) is charity; and every Tahlīl (saying: There is no God but Allah) is charity.'),
             cut(he(4558, 'ur'), 'بے شک ہر بار سبحان اللہ کہنا صدقہ ہے، ہر بار اللہ أکبر کہنا صدقہ ہے، ہر بار الحمد للہ کہنا صدقہ ہے اور ہر بار لاالہ الا اللہ کہنا صدقہ ہے')))

E['la-ilaha-illallahu-wallahu-akbar-wa'] = dict(
    phrase=P(cut(ha('tirmidhi', 3460, 'eng'), 'None has the right to be worshipped but Allah, and Allah is the Greatest, and there is no might nor power except by Allah'),
             cut(ha('tirmidhi', 3460, 'urd'), 'اللہ عزوجل کے سوا کوئی معبود برحق نہیں ہے اور اللہ تعالیٰ سب سے بڑا ہے، اور اللہ سبحانہ و تعالیٰ کے سوا کسی کام کے کرنے کی کسی میں نہ کوئی طاقت ہے اور نہ ہی قوت')),
    hadith=P(cut(ha('tirmidhi', 3460, 'eng'), 'there is not anyone upon the earth who says: ‘None has the right to be worshipped but Allah, and Allah is the Greatest, and there is no might nor power except by Allah, (Lā ilāha illallāh, wa Allāhu akbar, wa lā ḥawla wa lā quwwata illā billāh) except that his sins shall be pardoned, even if they were like the foam of the sea.'),
             cut(ha('tirmidhi', 3460, 'urd'), 'اس کے ( چھوٹے چھوٹے ) گناہ بخش دیئے جائیں گے، اگرچہ سمندر کی جھاگ کی طرح ( بہت زیادہ ) ہوں')))
N['la-ilaha-illallahu-wallahu-akbar-wa'] = ['Not in HadeethEnc or Hisn al-Muslim; all four texts are hadith-api (Tirmidhi 3460).']

E['five-declarations-of-oneness'] = dict(
    phrase=P(None, None),
    hadith=P(cut(he(6273, 'en'), "He (the Prophet) used to say: 'Anyone who says this in his illness then dies, the Fire will not consume him.'"),
             cut(he(6273, 'ur'), 'اور آپ فرماتے تھے جو ان کلمات کو اپنی بیماری میں کہے اور مر جائے تو (جہنم کی) آگ اسے نہ کھائے گی')),
    steps=True)
N['five-declarations-of-oneness'] = ['Whole-entry phrase left null: HadeethEnc interleaves Allah\'s replies between the five statements, so there is no contiguous published translation of the combined text; use the per-step translations. HadeethEnc English renders steps 2 and 3 as one clause ("there is no god but Allah alone; He has no partner"), so step 2 en is the first half of that clause.',
    'Also usable as hadith: the opening "Whoever says, \'there is no god but Allah, and Allah is Greatest,\' his Lord affirms it..." (same HadeethEnc #6273).']

E['ya-dhal-jalali-wal-ikram'] = dict(
    phrase=P(cut(he(3167, 'en'), 'O Possessor of Majesty and Bounty'), cut(he(10947, 'ur'), 'اے صاحب جلال و اکرام')),
    hadith=P(cut(he(3167, 'en'), 'Recite frequently: Yādha al-jalāl wa al-ikrām (O Possessor of Majesty and Bounty).'),
             cut(he(3167, 'ur'), 'تم لوگ (اپنی دعاؤں میں) کثرت کے ساتھ ”يَاذا الجَلاَلِ والإكْرامِ“ پڑھتے رہا کرو۔')))
N['ya-dhal-jalali-wal-ikram'] = ['phrase.ur is cut from HadeethEnc #10947 Urdu (Muslim 591, "...tabarakta dhal-jalali wal-ikram"); #3167 Urdu keeps the words in Arabic.']

E['raditu-billahi-rabba-wa-bil-islami'] = dict(
    phrase=P(cut(he(65906, 'en'), 'I am pleased with Allah as a Lord, with Islam as a religion, with Muhammad as a Messenger'),
             cut(ha('abudawud', 1529, 'urd'), 'میں اللہ کے رب ہونے، اسلام کے دین ہونے اور محمد صلی اللہ علیہ وسلم کے رسول ہونے پر راضی ہوا')),
    hadith=P(cut(he(65906, 'en'), 'Whoever says: Radītu billāhi rabban, wa bil-Islāmi dīnan, wa bi Muhammadin rasūla (I am pleased with Allah as a Lord, with Islam as a religion, with Muhammad as a Messenger), Paradise is guaranteed for him.'),
             cut(he(4193, 'ur'), 'جو اللہ کے رب ہونے پر، اسلام کے دین ہونے پر اور محمد ﷺ کے رسول ہونے پرراضی ہو گیا، اس کے لئے جنت واجب ہو گئی۔')))
N['raditu-billahi-rabba-wa-bil-islami'] = ['HadeethEnc #65906 (Abu Dawud 1529) has no Urdu. hadith.ur is HadeethEnc #4193 (Sahih Muslim 1884, the "cf." reference), and phrase.ur is the hadith-api Urdu of Abu Dawud 1529.']

E['ashhadu-an-la-ilaha-illallah-wa'] = dict(
    phrase=P(cut(he(10098, 'en'), 'bears witness that there is no god but Allah and that Muhammad is His slave and Messenger'),
             cut(he(10098, 'ur'), 'یہ گواہی دیتا ہے کہ اللہ کے سوا کوئی معبودِ برحق نہیں اور محمد ﷺ اس کے رسول ہیں')),
    hadith=P(cut(he(10098, 'en'), 'There is no one who bears witness that there is no god but Allah and that Muhammad is His slave and Messenger, sincerely from his heart, except that Allah will make him forbidden to Hellfire.'),
             cut(he(10098, 'ur'), 'جو بندہ سچے دل سے یہ گواہی دیتا ہے کہ اللہ کے سوا کوئی معبودِ برحق نہیں اور محمد ﷺ اس کے رسول ہیں، اسے اللہ جہنم پر حرام کر دیتا ہے')))
N['ashhadu-an-la-ilaha-illallah-wa'] = ['Both phrase texts are third person ("bears witness"), because no source has a first-person translation of this exact wording. HadeethEnc English adds "His slave and", which is not in the Arabic (وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ); see alternative.']
ALT['ashhadu-an-la-ilaha-illallah-wa'] = {'phrase.en': cut(ha('bukhari', 128, 'eng'), 'none has the right to be worshipped but Allah and Muhammad is his Apostle')}

E['subhanallah-walhamdulillah-wa-la-ilaha-illallah'] = dict(
    phrase=P(cut(he(6211, 'en'), 'Glory be to Allah, praise be to Allah, there is no god but Allah, and Allah is the Most Great'),
             cut(he(6211, 'ur'), 'اللہ پاک ہے، تمام تعریفیں اللہ کی ہیں، اللہ کے علاوہ اور کوئی معبود برحق نہیں اور اللہ ہی سب سے بڑا ہے')),
    hadith=P(cut(he(5475, 'en'), 'The most beloved speech to Allah are four: subhān Allah (glory be Allah), al-hamdulillāh (praise be to Allah), la ilāha illa Allah (there is no god but Allah), and Allāhu akbar (Allah is the Most Great), and it does not matter which one of them you start with.'),
             cut(he(5475, 'ur'), 'اللہ کے نزدیک سب سے محبوب کلام چار ہيں؛ سبحان اللہ (اللہ پاک ہے)، الحمد للہ (ساری تعریف اللہ کی ہے)، لا الہ الا اللہ (اللہ کے علاوہ کوئی معبود برحق نہيں ہے) اور اللہ اکبر (اللہ سب سے بڑا ہے)۔ تم ان میں سے جسے چاہو پہلے کہو، حرج کی کوئی بات نہیں ہے۔')))
N['subhanallah-walhamdulillah-wa-la-ilaha-illallah'] = ['Second virtue available: HadeethEnc #6211 (Muslim 2695, "more beloved to me than everything upon which the sun rises"), en+ur. #5475 English has the source typo "glory be Allah".']

E['subhanallah-walhamdulillah-wa-la-ilaha-illallah-2'] = dict(
    phrase=P(cut(he(10915, 'en'), 'Glory to Allah, praise to Allah, there is no god but Allah, Allah is the greatest, and there is no power and no strength but from Allah'),
             cut(ha('abudawud', 832, 'urd'), 'اللہ پاک ہے، اسی کی تعریف ہے، اس کے علاوہ اور کوئی معبود نہیں، اور اللہ سب سے بڑا ہے۔ برائیوں سے بچنا اور نیکی کی توفیق ملنا، اللہ کے سوا کسی سے ممکن نہیں۔')),
    hadith=P(cut(he(10915, 'en'), 'a man came to the Prophet (may Allah\'s peace and blessings be upon him) and said: "I cannot retain any of the Qur\'an. Teach me what suffices me."'),
             cut(he(10915, 'ur'), 'ایک آدمی نبی ﷺ کے پاس حاضر ہوکر کہنے لگا: میں قرآن مجید میں سے کچھ بھی یاد نہیں کر سکتا، مجھے کوئی ایسی چیز سکھا دیجیے، جو مجھے قرآن مجید کی جگہ کافی ہو سکے۔')))
N['subhanallah-walhamdulillah-wa-la-ilaha-illallah-2'] = ['HadeethEnc #10915 text ends "...billahil-\'Aliyyil-\'Azim"; phrase.en is cut before that addition to match the app\'s Arabic. HadeethEnc Urdu leaves the phrase in Arabic, so phrase.ur is hadith-api Abu Dawud 832 (cut the same way).',
    '"al-baqiyat as-salihat" virtue: HadeethEnc #5477 has en ("The lasting good deeds are the statements: ...") and ur.']

E['la-hawla-wa-la-quwwata-illa'] = dict(
    phrase=P(cut(he(6273, 'en'), 'there is no might nor power save with Allah'), cut(he(6273, 'ur'), 'گناہ سے بچنے اور بھلے کام کرنے کی طاقت نہیں ہے، مگر اللہ تعالیٰ کی توفیق سے')),
    hadith=P(cut(hisn(130, 260), 'shall I not inform you of a treasure from the treasures of paradise?’ He then said: ‘Say: (There is no might nor power except with Allah.)'),
             cut(ha('bukhari', 6384, 'urd'), 'عبداللہ بن قیس کہو «لا حول ولا قوة إلا بالله» کیونکہ یہ جنت کے خزانوں میں سے ایک خزانہ ہے')))
N['la-hawla-wa-la-quwwata-illa'] = ['"Treasure of Paradise" hadith is not in HadeethEnc; hadith.en is Hisn al-Muslim and hadith.ur is hadith-api Bukhari 6384.']

E['la-ilaha-illallahu-wahdahu-la-sharika-2'] = dict(
    phrase=P(cut(he(6112, 'en'), 'There is no god but Allah, alone with no partner. Allah is the Greatest with all greatness, and abundant praise is due to Allah. Allah, the Lord of the worlds, is exalted above imperfection. There is no might nor power except with Allah, the Mighty and the All-Wise'),
             cut(he(6112, 'ur'), 'اللہ کے سوا کوئی حقیقی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اللہ بڑائی میں سب سے بڑا ہے اور بے حد و حساب تعریف اللہ کی ہے، اللہ ہر عیب اور نقص سے پاک ہے، جو کہ تمام جہانوں کا رب ہے، گناہ سے بچنے اور نیکی کے کام کرنے کی طاقت اللہ ہی سے مل سکتی ہے، جو غالب حکمت والا ہے')),
    hadith=P(cut(he(6112, 'en'), 'A Bedouin came to the Messenger of Allah (may Allah\'s peace and blessings be upon him) and said: Teach me words to say.'),
             cut(he(6112, 'ur'), 'ایک اعرابی رسول اللہ صلی اللہ علیہ وسلم کی خدمت میں حاضر ہوا اور عرض کیا کہ مجھے ایسا کلام سکھائیے، جسے میں پڑھتا رہوں۔')))

E['allahu-akbaru-kabira-walhamdulillahi-kathira-wa'] = dict(
    phrase=P(cut(ha('muslim', 601, 'eng'), 'Allah is truly Great, praise be to Allah in abundance. Glory be to Allah in the morning and the evening.'),
             cut(ha('muslim', 601, 'urd'), 'اللہ سب سے بڑا ہے بہت بڑا ، اور تمام تعریف اللہ کے لیے ہے بہت زیادہ اور تسبیح اللہ ہی کے لیے ہے ، صبح و شام ۔')),
    hadith=P(cut(ha('muslim', 601, 'eng'), 'He (the Holy Prophet) said: It (its utterance) surprised me, for the doors of heaven were opened for It.'),
             cut(ha('muslim', 601, 'urd'), 'آپ نے فرمایا ’’مجھے ان پر بہت حیرت ہوئی ، ان کے لیے آسمان کے دروازے کھول دیے گئے ۔')))
N['allahu-akbaru-kabira-walhamdulillahi-kathira-wa'] = ['Not in HadeethEnc or Hisn al-Muslim; all four texts are hadith-api (Muslim 601).']

E['100-each-as-taught-to-umm'] = dict(
    phrase=P(None, None),
    hadith=P(cut(ha('ibnmajah', 3810, 'eng'), "Proclaim the greatness of Allah (say Allahu Akbar) one hundred times, praise Allah (say Al-Hamdu Lillah) one hundred times, and glorify Allah (say Subhan-Allah) one hundred times. (That is) better than one hundred horses bridled and saddled for the sake of Allah, better than one hundred sacrificial camels, and better than (freeing) one hundred slaves"),
             cut(ha('ibnmajah', 3810, 'urd'), 'سو بار «اللہ اکبر»، سو بار «الحمدللہ»، سو بار «سبحان اللہ»کہو، یہ ان سو گھوڑوں سے بہتر ہے جو جہاد فی سبیل اللہ میں مع زین و لگام کے کس دیئے جائیں، اور سو جانور قربان کرنے، اور سو غلام آزاد کرنے سے بہتر ہیں ۔')),
    steps=True)
N['100-each-as-taught-to-umm'] = ['Whole-entry phrase null (it is three separate phrases); see steps. hadith is hadith-api only (Ibn Majah 3810).']

E['allahumma-anta-rabbi-la-ilaha-illa'] = dict(
    phrase=P(cut(he(5503, 'en'), 'O Allah, You are my Lord. You created me, and I am Your slave. I will remain faithful to Your covenant and promise as much as possible. I seek refuge with You from the evil of what I have done. I acknowledge Your favor upon me, and I admit my sin. So, forgive me. Indeed, none can forgive sins but You'),
             cut(he(5503, 'ur'), 'اے اللہ ! تو میرا رب ہے، تیرے سوا کوئی معبود برحق نہیں۔ تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں۔ میں اپنی طاقت کے مطابق تجھ سے کیے ہوئے عہد اور وعدے پر قائم ہوں۔ میں اپنے کیے ہوئے اعمال کے شر سے تیری پناہ مانگتا ہوں۔ میں تیرے حضور تیری جانب سے ملنے والی نعمتوں کا اقرار کرتا ہوں۔ ایسے ہی اپنے گناہوں کا بھی اعتراف کرتا ہوں۔ لہذا میرى مغرفت فرما، کیوں کہ تیرے سوا کوئی گناہوں کى مغفرت کرنے والا نہیں ہے۔')),
    hadith=P(cut(he(5503, 'en'), 'And whoever says this during the day while being certain of its meaning, then he dies before the evening, he will be from the people of Paradise, and whoever says it at night while being certain of its meaning then he dies before the morning, he will be from the people of Paradise.'),
             cut(he(5503, 'ur'), 'جس شخص نے کامل یقین کے ساتھ دن کے وقت اسے پڑھا اور اسی دن شام ہونے سے پہلے اس کی موت ہوگئی، وہ جنتی ہے۔ اسی طرح جس نے رات کے وقت کامل یقین کے ساتھ اسے پڑھا اور صبح ہونے سے قبل ہی فوت ہو گیا، تو وہ بھی جنتی ہے۔')))
N['allahumma-anta-rabbi-la-ilaha-illa'] = ['HadeethEnc English leaves out "la ilaha illa ant" (there is no god but You); the Hisn al-Muslim English (alternative) is complete. The Urdu contains the source typo "مغرفت".']
ALT['allahumma-anta-rabbi-la-ilaha-illa'] = {'phrase.en': cut(hisn(27, 79), 'O Allah, You are my Lord, none has the right to be worshipped except You, You created me and I am Your servant and I abide to Your covenant and promise as best I can, I take refuge in You from the evil of which I have committed.  I acknowledge Your favour upon me and I acknowledge my sin, so forgive me, for verily none can forgive sin except You.')}

E['astaghfirullah'] = dict(
    phrase=P(cut(he(10947, 'en'), 'I seek forgiveness from Allah'), cut(he(10576, 'ur'), 'میں اللہ تعالیٰ سے بخشش طلب کرتا ہوں')),
    hadith=P(cut(hisn(129, 253), 'verily my heart becomes preoccupied, and verily I seek Allah’s forgiveness a hundred times a day.'),
             cut(ha('muslim', 2702, 'urd', '2702.01'), 'میرے دل پر غبار سا چھا جاتا ہے تو میں ( اس کیفیت کے ازالے کے لیے ) ایک دن میں سو بار اللہ سے استغفار کرتا ہوں ۔')))
N['astaghfirullah'] = ['Muslim 2702 (the 100-times hadith) is not in HadeethEnc; hadith.en is Hisn al-Muslim and hadith.ur is hadith-api. For after-prayer istighfar (x3), HadeethEnc #10947 (Muslim 591) has en+ur.']

E['astaghfirullaha-wa-atubu-ilayh'] = dict(
    phrase=P(cut(he(4808, 'en'), 'I ask Allah for forgiveness and repent to Him'), cut(he(4808, 'ur'), 'اللہ سے استغفار اور توبہ کرتا ہوں')),
    hadith=P(cut(he(4808, 'en'), 'By Allah! I ask Allah for forgiveness and repent to Him more than seventy times a day.'),
             cut(he(4808, 'ur'), 'اللہ کی قسم! مین دن میں ستر سے زائد مرتبہ اللہ سے استغفار اور توبہ کرتا ہوں')))
N['astaghfirullaha-wa-atubu-ilayh'] = ['The Urdu has the source typo "مین" (for میں). 100-times repentance: HadeethEnc #4809 (Muslim 2702) has en+ur.']

E['rabbighfir-li-wa-tub-alayya-innaka'] = dict(
    phrase=P(cut(ha('ibnmajah', 3814, 'eng'), 'O Allah forgive me and accept my repentance, for You are the Accepter of repentance, the Most Merciful'),
             cut(ha('abudawud', 1516, 'urd'), 'اے میرے رب! مجھے بخش دے، میری توبہ قبول کر، تو ہی توبہ قبول کرنے والا اور رحم فرمانے والا ہے')),
    hadith=P(cut(ha('ibnmajah', 3814, 'eng'), 'We used to count that the Messenger of Allah (ﷺ) said one hundred times in a gathering:'),
             cut(ha('abudawud', 1516, 'urd'), 'عبداللہ بن عمر رضی اللہ عنہما کہتے ہیں کہ ہم ایک مجلس میں رسول اللہ صلی اللہ علیہ وسلم کے سو بار: «رب اغفر لي وتب علي إنك أنت التواب الرحيم» اے میرے رب! مجھے بخش دے، میری توبہ قبول کر، تو ہی توبہ قبول کرنے والا اور رحم فرمانے والا ہے کہنے کو شمار کرتے تھے۔')))
N['rabbighfir-li-wa-tub-alayya-innaka'] = ['Not in HadeethEnc. Hisn al-Muslim (ch. 84 #195) has only the Tirmidhi wording ending "at-Tawwabul-Ghafur", so it was not used for the app\'s "at-Tawwabur-Rahim" wording. All texts are hadith-api.']

E['subhanallahi-wa-bihamdihi-astaghfirullaha-wa-atubu'] = dict(
    phrase=P(cut(ha('muslim', 484, 'eng', '484.04'), 'Hallowed be Allah and with His praise, I seek the forgiveness of Allah and return to Him'),
             cut(ha('muslim', 484, 'urd', '484.04'), 'میں اللہ کی پاکیزگی بیان کرتا ہوں اس کی حمد کے ساتھ ، میں اللہ سے بخشش کا طلب گار ہوں اور اسی کی طرف رجوع کرتا ہوں')),
    hadith=P(cut(ha('muslim', 484, 'eng', '484.04'), 'The Messenger of Allah (ﷺ) recited often these words: Hallowed be Allah and with His praise, I seek the forgiveness of Allah and return to Him.'),
             cut(ha('muslim', 484, 'urd', '484.04'), 'رسول اللہﷺ کثرت سے یہ فرمایا کرتے تھے : ’’میں اللہ کی پاکیزگی بیان کرتا ہوں اس کی حمد کے ساتھ ، میں اللہ سے بخشش کا طلب گار ہوں اور اسی کی طرف رجوع کرتا ہوں ۔')))
N['subhanallahi-wa-bihamdihi-astaghfirullaha-wa-atubu'] = ['Not in HadeethEnc or Hisn al-Muslim; all texts are hadith-api (Muslim 484, 4th narration).']

E['astaghfirullahal-azimalladhi-la-ilaha-illa-huwal'] = dict(
    phrase=P(cut(hisn(129, 250), 'I seek Allah’s forgiveness, besides whom, none has the right to be worshipped except He, The Ever Living, The Self-Subsisting and Supporter of all, and I turn to Him in repentance.'),
             cut(ha('tirmidhi', 3577, 'urd'), 'میں مغفرت مانگتا ہوں اس بزرگ و برتر اللہ سے جس کے سوا کوئی معبود برحق نہیں ہے، جو زندہ ہے اور ہر چیز کا نگہبان ہے اور میں اسی کی طرف رجوع کرتا ہوں')),
    hadith=P(cut(he(10576, 'en'), "his sins will be forgiven even if he has fled from the battlefield."),
             cut(he(10576, 'ur'), 'اس کی مغفرت کر دی جاتی ہے، اگرچہ وہ میدان جہاد سے ہی فرار کیوں نہ ہوا ہو۔')))
N['astaghfirullahal-azimalladhi-la-ilaha-illa-huwal'] = ['HadeethEnc #10576 uses the wording without "al-\'Azim", so it supplies only the hadith. The Hisn Arabic includes al-\'Azim but its English leaves it out; the alternative (Tirmidhi 3577 via hadith-api) translates it.']
ALT['astaghfirullahal-azimalladhi-la-ilaha-illa-huwal'] = {'phrase.en': cut(ha('tirmidhi', 3577, 'eng'), 'I seek forgiveness from Allah, the Magnificent, whom there is none worthy of worship but Him, the Living, Al-Qayyum, and I repent to him')}

E['allahumma-inni-zalamtu-nafsi-zulman-kathira'] = dict(
    phrase=P(cut(hisn(24, 57), 'O Allah, I have indeed oppressed my soul excessively and none can forgive sin except You, so forgive me a forgiveness from Yourself and have mercy upon me.  Surely, You are The Most-Forgiving, The Most-Merciful.'),
             cut(ha('bukhari', 834, 'urd'), 'اے اللہ! میں نے اپنی جان پر ( گناہ کر کے ) بہت زیادہ ظلم کیا پس گناہوں کو تیرے سوا کوئی دوسرا معاف کرنے والا نہیں۔ مجھے اپنے پاس سے بھرپور مغفرت عطا فرما اور مجھ پر رحم کر کہ مغفرت کرنے والا اور رحم کرنے والا بیشک وشبہ تو ہی ہے۔')),
    hadith=P(cut(ha('bukhari', 834, 'eng'), "I asked Allah's Messenger (ﷺ) to teach me an invocation so that I may invoke Allah with it in my prayer. He told me to say,"),
             cut(ha('bukhari', 834, 'urd'), 'انہوں نے رسول اللہ صلی اللہ علیہ وسلم سے عرض کیا کہ آپ صلی اللہ علیہ وسلم مجھے کوئی ایسی دعا سکھا دیجئیے جسے میں نماز میں پڑھا کروں۔')))
N['allahumma-inni-zalamtu-nafsi-zulman-kathira'] = ['Not in HadeethEnc. phrase.en is Hisn al-Muslim; the rest is hadith-api (Bukhari 834).']

E['allahumma-innaka-afuwwun-tuhibbul-afwa-fafu'] = dict(
    phrase=P(cut(ha('ibnmajah', 3850, 'eng'), 'O Allah, You are Forgiving and love forgiveness, so forgive me'),
             cut(ha('ibnmajah', 3850, 'urd'), 'اے اللہ تو معاف کرنے والا ہے اور معافی و درگزر کو پسند کرتا ہے تو تو مجھ کو معاف فرما دے')),
    hadith=P(cut(ha('ibnmajah', 3850, 'eng'), '"O Messenger of Allah, what do you think I should say in my supplication, if I come upon Laylatul-Qadr?" He said: "Say: \'Allahumma innaka \'afuwwun tuhibbul-\'afwa, fa\'fu \'anni (O Allah, You are Forgiving and love forgiveness, so forgive me)'),
             cut(ha('ibnmajah', 3850, 'urd'), 'اللہ کے رسول! اگر مجھے شب قدر مل جائے تو کیا دعا کروں؟ آپ صلی اللہ علیہ وسلم نے فرمایا: یہ دعا کرو «اللهم إنك عفو تحب العفو فاعف عني» اے اللہ تو معاف کرنے والا ہے اور معافی و درگزر کو پسند کرتا ہے تو تو مجھ کو معاف فرما دے ۔')))
N['allahumma-innaka-afuwwun-tuhibbul-afwa-fafu'] = ['Not in HadeethEnc or Hisn al-Muslim; all texts are hadith-api (Ibn Majah 3850).']

E['subhanaka-allahumma-wa-bihamdika-ashhadu-an'] = dict(
    phrase=P(cut(hisn(85, 196), 'How perfect You are O Allah, and I praise You.  I bear witness that None has the right to be worshipped except You.  I seek Your forgiveness and turn to You in repentance.'),
             cut(ha('tirmidhi', 3433, 'urd'), 'پاک ہے تو اے اللہ! اور سب تعریف تیرے لیے ہے، میں گواہی دیتا ہوں کہ تیرے سوا کوئی معبود برحق نہیں، میں تجھ سے مغفرت چاہتا ہوں اور تیری طرف رجوع کرتا ہوں')),
    hadith=P(cut(ha('tirmidhi', 3433, 'eng'), 'Whoever sits in a sitting and engages in much empty, meaningless speech and then says before getting from that sitting of his: ‘Glory is to You, O Allah, and praise, I bear witness that there is none worthy of worship except You, I seek You forgiveness, and I repent to You, (Subḥānaka Allāhumma wa biḥamdika, ashhadu an lā ilāha illā anta, astaghfiruka wa atūbu ilaik)’ whatever occurred in that sitting would be forgiven to him.'),
             cut(ha('tirmidhi', 3433, 'urd'), 'جو شخص کسی مجلس میں بیٹھے اور اس سے بہت سی لغو اور بیہودہ باتیں ہو جائیں، اور وہ اپنی مجلس سے اٹھ جانے سے پہلے پڑھ لے: «سبحانك اللهم وبحمدك أشهد أن لا إله إلا أنت أستغفرك وأتوب إليك» ”پاک ہے تو اے اللہ! اور سب تعریف تیرے لیے ہے، میں گواہی دیتا ہوں کہ تیرے سوا کوئی معبود برحق نہیں، میں تجھ سے مغفرت چاہتا ہوں اور تیری طرف رجوع کرتا ہوں“، تو اس کی اس مجلس میں اس سے ہونے والی لغزشیں معاف کر دی جاتی ہیں')))
N['subhanaka-allahumma-wa-bihamdika-ashhadu-an'] = ['Not in HadeethEnc. phrase.en is Hisn al-Muslim; the rest is hadith-api (Tirmidhi 3433).']

E['allahumma-salli-ala-muhammad'] = dict(
    phrase=P(cut(he(5377, 'en'), 'O Allah, bestow Your grace upon Muhammad'), cut(he(5377, 'ur'), 'اے اللہ ! محمد (ﷺ) پر اپنی رحمت نازل کر')),
    hadith=P(cut(he(65087, 'en'), "whoever invokes Allah's blessings upon me once, Allah will bestow His blessings upon him ten times"),
             cut(he(65087, 'ur'), 'جو مجھ پر ایک دفعہ درود بھیجتا ہے، اللہ تعالیٰ اس کے بدلے میں فرشتوں کے سامنے دس بار اس کی تعریف کرتا ہے')))
N['allahumma-salli-ala-muhammad'] = ['Virtue text is from HadeethEnc #65087 (Sahih Muslim 384), which states the same "one blessing, ten from Allah" virtue as Muslim 408. Muslim 408 itself is not in HadeethEnc; Hisn al-Muslim ch. 107 #219 has an English version ("Whoever sends a prayer upon me, Allah sends ten upon him.").',
    'HadeethEnc #6181 (Tirmidhi 2457) has en+ur for the "your worries will be sufficed" virtue.']

E['allahumma-salli-ala-muhammadin-wa-ala'] = dict(
    phrase=P(cut(hisn(23, 53), 'O Allah, send prayers upon Muhammad and the followers of Muhammad, just as You sent prayers upon Ibraheem and upon the followers of Ibraheem.  Verily, You are full of praise and majesty. O Allah, send blessings upon Mohammad and upon the family of Muhammad, just as You sent blessings upon Ibraheem and upon the family of Ibraheem.  Verily, You are full of praise and majesty.'),
             cut(ha('bukhari', 3370, 'urd'), 'اے اللہ! اپنی رحمت نازل فرما محمد صلی اللہ علیہ وسلم پر اور آل محمد صلی اللہ علیہ وسلم پر جیسا کہ تو نے اپنی رحمت نازل فرمائی ابراہیم پر اور آل ابراہیم علیہ السلام پر۔ بیشک تو بڑی خوبیوں والا اور بزرگی والا ہے۔ اے اللہ! برکت نازل فرما محمد پر اور آل محمد پر جیسا کہ تو نے برکت نازل فرمائی ابراہیم پر اور آل ابراہیم پر۔ بیشک تو بڑی خوبیوں والا اور بڑی عظمت والا ہے۔')),
    hadith=P(cut(he(5377, 'en'), 'we said: O Messenger of Allah, we have learned how to greet you, but how should we invoke the blessings of Allah upon you? He said: "Say:'),
             cut(he(5377, 'ur'), 'ہم نے عرض کیا : اے اللہ کے رسول! ہم یہ تو جان گئے ہیں کہ ہم آپ پر سلام کیسے بھیجیں، لیکن (یہ نہيں معلوم کہ) ہم آپ پر درود کیسے بھیجیں؟')))
N['allahumma-salli-ala-muhammadin-wa-ala'] = ['HadeethEnc #5377 (Bukhari 6357 wording) says "ka-ma sallayta \'ala ali Ibrahim" without "\'ala Ibrahima wa", so it supplies only the hadith. phrase.en is Hisn al-Muslim (exact wording of Bukhari 3370) and phrase.ur is hadith-api Bukhari 3370.']

E['allahumma-salli-ala-muhammadin-wa-azwajihi'] = dict(
    phrase=P(cut(hisn(23, 54), 'O Allah, send prayers upon Muhammad and upon the wives and descendants of Muhammad, just as You sent prayers upon the family of Ibraheem, and send blessings upon Muhammad and upon the wives and descendants of Muhammad, just as You sent blessings upon the family of Ibraheem. Verily, You are full of praise and majesty.'),
             cut(ha('bukhari', 3369, 'urd'), 'اے اللہ! رحمت نازل فرما محمد پر اور ان کی بیویوں پر اور ان کی اولاد پر جیسا کہ تو نے رحمت نازل فرمائی ابراہیم پر اور اپنی برکت نازل فرما محمد پر اور ان کی بیویوں اور اولاد پر جیسا کہ تو نے برکت نازل فرمائی آل ابراہیم پر۔ بیشک تو انتہائی خوبیوں والا اور عظمت والا ہے۔')),
    hadith=P(cut(ha('bukhari', 3369, 'eng'), 'The people asked, "O Allah\'s Messenger (ﷺ)! How shall we (ask Allah to) send blessings on you?" Allah\'s Apostle replied, "Say:'),
             cut(ha('bukhari', 3369, 'urd'), 'صحابہ نے عرض کیا: یا رسول اللہ! ہم آپ پر کس طرح درود بھیجا کریں؟')))
N['allahumma-salli-ala-muhammadin-wa-azwajihi'] = ['Not in HadeethEnc. phrase.en is Hisn al-Muslim; the rest is hadith-api (Bukhari 3369).']

E['allahumma-salli-ala-muhammadin-abdika-wa'] = dict(
    phrase=P(None, cut(ha('bukhari', 6358, 'urd'), 'اے اللہ! اپنی رحمت نازل کر محمد ( صلی اللہ علیہ وسلم ) پر جو تیرے بندے ہیں اور تیرے رسول ہیں جس طرح تو نے رحمت نازل کی ابراہیم پر اور برکت بھیج محمد ( صلی اللہ علیہ وسلم ) پر اور ان کی آل پر جس طرح برکت بھیجی تو نے ابراہم پر اور آل ابراہیم پر۔')),
    hadith=P(cut(ha('bukhari', 6358, 'eng'), 'We said, "O Allah\'s Messenger (ﷺ) This is (i.e. we know) the greeting to you; will you tell us how to send Salat on you?"'),
             cut(ha('bukhari', 6358, 'urd'), 'ہم نے کہا: اے اللہ کے رسول! آپ کو سلام اس طرح کیا جاتا ہے، لیکن آپ پر درود کس طرح بھیجا جاتا ہے؟')))
N['allahumma-salli-ala-muhammadin-abdika-wa'] = ['phrase.en null: the only published English found (Muhsin Khan, Bukhari 6358) transliterates the wording without translating it; the entry is not in HadeethEnc or Hisn al-Muslim.']

E['after-every-prayer-33-33-33'] = dict(
    phrase=P(None, None), steps=True,
    hadith=P(cut(he(10948, 'en'), 'Whoever glorifies Allah directly after each prayer thirty-three times, praises Allah thirty-three times, and proclaims the greatness of Allah thirty-three times, these are ninety-nine, and completes one hundred by saying: La ilāha illallāh wahdahu la sharīka lah, lahu al-mulku wa lahu al-hamdu wa huwa ‘ala kulli shay’in qadīr (There is no god except Allah. He is One and has no partner with Him. To Him belongs sovereignty and to Him belongs praise, and He is Omnipotent over everything), his sins will be forgiven even if they are like the sea foam.'),
             cut(he(10948, 'ur'), 'جو شخص ہر نماز کے بعد تینتیس مرتبہ سبحان اللہ، تینتیس مرتبہ الحمدللہ اور تینتیس مرتبہ اللہ اکبر کہے، تو یہ کل ننانوے مرتبہ ہوا اور سو کی عدد پورا کرتے ہوئے ‘‘لا إله إلا الله وحْدَه لا شريك له له المُلك وله الحَمد وهو على كلِّ شيء قَدِير’’ (اللہ کے سوا کوئی معبود بر حق نہيں ہے، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہت ہے، اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے) کہے، تو اس کے گناہ بخش دیے جائیں گے، اگرچہ وہ سمندر کی جھاگ کی طرح ہوں')))

E['after-every-prayer-33-33-and'] = dict(
    phrase=P(None, None), steps=True,
    hadith=P(cut(he(6259, 'en'), "There are certain things, the sayer of which – or the performer of which – after every prescribed prayer will never be disappointed: Tasbīh (saying 'Subhānallāh') thirty-three times, Tahmīd (saying 'Al-hamdulillāh') thirty-three times, and Takbīr (saying 'Allāhu Akbar') thirty-four times."),
             cut(he(6259, 'ur'), 'نماز کے بعد کی کچھ ایسی دعائیں ہیں کہ انھیں ہر فرض نماز کے بعد پڑھنے والا یا انہیں بجا لانے والا (ثواب سے یا بلند درجوں سے) محروم نہیں ہوتا۔ (اور وہ یہ ہیں) تینتیس بار سبحان اللہ اور تینتیس بار الحمدللہ اور چونتیس بار اللہ اکبر کہنا۔')))

E['after-every-prayer-10-each'] = dict(
    phrase=P(None, None), steps=True,
    hadith=P(cut(ha('abudawud', 5065, 'eng'), 'There are two qualities or characteristics which will not be returned by any Muslim without his entering Paradise. While they are easy, those who act upon them are few. One should say: "Glory be to Allah" ten times after every prayer, "Praise be to Allah" ten times and "Allah is Most Great" ten times. That is a hundred and fifty on the tongue, but one thousand and five hundred on the scale.'),
             cut(ha('abudawud', 5065, 'urd'), 'دو خصلتیں یا دو عادتیں ایسی ہیں جو کوئی مسلم بندہ پابندی سے انہیں ( برابر ) کرتا رہے گا تو وہ ضرور جنت میں داخل ہو گا، یہ دونوں آسان ہیں اور ان پر عمل کرنے والے لوگ تھوڑے ہیں ( ۱ ) ہر نماز کے بعد دس بار «سبحان الله» اور دس بار «الحمد الله» اور دس بار «الله اكبر» کہنا، اس طرح یہ زبان سے دن اور رات میں ایک سو پچاس بار ہوئے، اور قیامت میں میزان میں ایک ہزار پانچ سو بار ہوں گے')))
N['after-every-prayer-10-each'] = ['Not in HadeethEnc or Hisn al-Muslim; hadith is hadith-api (Abu Dawud 5065).']

E['after-every-prayer-25-each'] = dict(
    phrase=P(None, None), steps=True,
    hadith=P(cut(ha('nasai', 1350, 'eng'), "'Instead of that, say each one twenty-five times, and include the tahlil among them.' The next morning he came to the Messenger of Allah (ﷺ) and told him about that, and he said: 'Do that"),
             cut(ha('nasai', 1350, 'urd'), 'تم انہیں پچیس، پچیس بار کر لو، اور باقی پچیس کی جگہ «لا الٰہ إلا اللہ» کہا کرو، تو جب صبح ہوئی تو وہ نبی اکرم صلی اللہ علیہ وسلم کے پاس آئے، اور آپ سے سارا واقعہ بیان کیا، تو آپ نے فرمایا: اسے اسی طرح کر لو ۔')))
N['after-every-prayer-25-each'] = ['Not in HadeethEnc or Hisn al-Muslim; hadith is hadith-api (Nasa\'i 1350).']

E['before-sleep-33-33-and-34'] = dict(
    phrase=P(None, None), steps=True,
    hadith=P(cut(he(6076, 'en'), "Let me guide you to something better than what you asked for. When you go to bed, say ‘Subhān Allah' (glory be to Allah) thirty-three times, ‘Alhamdulillāh' (praise be to Allah) thirty-three times, and ‘Allāhu Akbar' (Allah is the Most Great) thirty-four times. This is better for you than a servant."),
             cut(he(6076, 'ur'), 'کیا میں تمھیں جو کچھ تم نے مانگا ہے، اس سے بہتر چیز نہ بتاؤں؟ جب تم دونوں اپنے بستر پر جاؤ، یا فرمایا کہ جب تم سونے کے لیے جاؤ، تو تینتیس مرتبہ (33) سبحان اللہ، تینتیس مرتبہ (33) الحمد للہ اور چونتیس مرتبہ (34) اللہ اکبر پڑھ لینا۔ یہ تمھارے لیے خادم سے بہتر ہے۔')))

def Q(s, v, en, ur):
    return P(cut(qr(s, v, 'en'), en) if en else None, cut(qr(s, v, 'ur'), ur) if ur else None)

E['la-ilaha-illa-anta-subhanaka-inni'] = dict(
    phrase=Q(21, 87, 'There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.', 'تیرے سوا کوئی معبود نہیں۔ تو پاک ہے (اور) بےشک میں قصوروار ہوں'),
    hadith=P(cut(ha('tirmidhi', 3505, 'eng'), 'So indeed, no Muslim man supplicates with it for anything, ever, except Allah responds to him.'),
             cut(ha('tirmidhi', 3505, 'urd'), 'کیونکہ یہ ایسی دعا ہے کہ جب بھی کوئی مسلمان شخص اسے پڑھ کر دعا کرے گا تو اللہ تعالیٰ اس کی دعا قبول فرمائے گا')))
N['la-ilaha-illa-anta-subhanaka-inni'] = ['Tirmidhi 3505 is not in HadeethEnc or Hisn al-Muslim (Hisn ch. 35 #124 has the words only); hadith is hadith-api.']

E['hasbunallahu-wa-nimal-wakil'] = dict(
    phrase=Q(3, 173, 'Sufficient for us is Allah, and [He is] the best Disposer of affairs.', 'ہم کو خدا کافی ہے اور وہ بہت اچھا کارساز ہے'),
    hadith=P(cut(ha('bukhari', 4563, 'eng'), '\'Allah is Sufficient for us and He Is the Best Disposer of affairs," was said by Abraham when he was thrown into the fire; and it was said by Muhammad when they (i.e. hypocrites) said, "A great army is gathering against you, therefore, fear them,"'),
             cut(ha('bukhari', 4563, 'urd'), 'کلمہ «حسبنا الله ونعم الوكيل‏» ابراہیم علیہ السلام نے کہا تھا، اس وقت جب ان کو آگ میں ڈالا گیا تھا اور یہی کلمہ محمد صلی اللہ علیہ وسلم نے اس وقت کہا تھا جب لوگوں نے مسلمانوں کو ڈرانے کے لیے کہا تھا کہ لوگوں ( یعنی قریش ) نے تمہارے خلاف بڑا سامان جنگ اکٹھا کر رکھا ہے، ان سے ڈرو')))
N['hasbunallahu-wa-nimal-wakil'] = ['Bukhari 4563 is not in HadeethEnc; hadith is hadith-api.']

E['hasbiyallahu-la-ilaha-illa-huwa-alayhi'] = dict(
    phrase=Q(9, 129, 'Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.', 'خدا مجھے کفایت کرتا ہے اس کے سوا کوئی معبود نہیں اسی پر میرا بھروسہ ہے اور وہی عرش عظیم کا مالک ہے'),
    hadith=None)
E['subhana-rabbika-rabbil-izzati-amma-yasifun'] = dict(
    phrase=Q(37, [180, 181, 182], 'Exalted is your Lord, the Lord of might, above what they describe. And peace upon the messengers. And praise to Allah, Lord of the worlds.',
             'یہ جو کچھ بیان کرتے ہیں تمہارا پروردگار جو صاحب عزت ہے اس سے (پاک ہے) اور پیغمبروں پر سلام اور سب طرح کی تعریف خدائے رب العالمین کو (سزاوار) ہے'),
    hadith=None)
N['subhana-rabbika-rabbil-izzati-amma-yasifun'] = ['Three consecutive verse translations joined with a single space (no words changed).']
E['ma-sha-allah-la-quwwata-illa'] = dict(phrase=Q(18, 39, "What Allah willed [has occurred]; there is no power except in Allah", None), hadith=None)
N['ma-sha-allah-la-quwwata-illa'] = ['phrase.ur null: Jalandhry leaves the words untranslated ("تم نے ماشاالله لاقوة الابالله کیوں نہ کہا"). Published alternative: Muhammad Junagarhi (ur.junagarhi), given under alternatives.']
ALT['ma-sha-allah-la-quwwata-illa'] = {'phrase.ur': cut(qr(18, 39, 'ur-alt'), 'اللہ کا چاہا ہونے واﻻ ہے، کوئی طاقت نہیں مگر اللہ کی مدد سے')}
E['alhamdulillahi-rabbil-alamin'] = dict(phrase=Q(1, 2, '[All] praise is [due] to Allah, Lord of the worlds', 'سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے'), hadith=None)
E['alhamdulillahil-ladhi-lam-yattakhidh-waladan-wa'] = dict(
    phrase=Q(17, 111, 'Praise to Allah, who has not taken a son and has had no partner in [His] dominion and has no [need of a] protector out of weakness; and glorify Him with [great] glorification.',
             'سب تعریف خدا ہی کو ہے جس نے نہ تو کسی کو بیٹا بنایا ہے اور نہ اس کی بادشاہی میں کوئی شریک ہے اور نہ اس وجہ سے کہ وہ عاجز وناتواں ہے کوئی اس کا مددگار ہے اور اس کو بڑا جان کر اس کی بڑائی کرتے رہو'),
    hadith=None)
E['rabbana-zalamna-anfusana-wa-in-lam'] = dict(
    phrase=Q(7, 23, 'Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers.',
             'پروردگار ہم نے اپنی جانوں پر ظلم کیا اور اگر تو ہمیں نہیں بخشے گا اور ہم پر رحم نہیں کرے گا تو ہم تباہ ہو جائیں گے'), hadith=None)
E['rabbi-inni-zalamtu-nafsi-faghfir-li'] = dict(phrase=Q(28, 16, 'My Lord, indeed I have wronged myself, so forgive me', 'اے پروردگار میں نے اپنے آپ پر ظلم کیا تو مجھے بخش دے'), hadith=None)
E['rabbighfir-warham-wa-anta-khayrur-rahimin'] = dict(
    phrase=Q(23, 118, 'My Lord, forgive and have mercy, and You are the best of the merciful.', 'میرے پروردگار مجھے بخش دے اور (مجھ پر) رحم کر اور تو سب سے بہتر رحم کرنے والا ہے'), hadith=None)

E['rabbana-atina-fid-dunya-hasanatan-wa'] = dict(
    phrase=Q(2, 201, 'Our Lord, give us in this world [that which is] good and in the Hereafter [that which is] good and protect us from the punishment of the Fire.',
             'پروردگار ہم کو دنیا میں بھی نعمت عطا فرما اور آخرت میں بھی نعمت بخشیو اور دوزخ کے عذاب سے محفوظ رکھیو'),
    hadith=P(cut(he(5502, 'en'), 'The supplication that the Prophet (may Allah\'s peace and blessings be upon him) recited most was:'),
             cut(he(5502, 'ur'), 'اللہ کے نبی ﷺ اکثر یہ دعا کیا کرتے تھے')))
E['ya-muqallibal-qulub-thabbit-qalbi-ala'] = dict(
    phrase=P(cut(he(3142, 'en'), 'O Turner of the hearts, make my heart firm upon Your religion.'), cut(he(3142, 'ur'), 'اے دلوں کو پھیرنے والے! میرے دل کو اپنے دین پر ثابت قدم رکھ')),
    hadith=P(cut(he(3142, 'en'), 'The Messenger of Allah (may Allah\'s peace and blessings be upon him) would often say: "O Turner of the hearts, make my heart firm upon Your religion."'),
             cut(he(3142, 'ur'), 'اللہ کے رسول صلی اللہ علیہ و سلم اکثر یہ دعا کیا کرتے تھے : "اے دلوں کو پھیرنے والے! میرے دل کو اپنے دین پر ثابت قدم رکھ"۔')))
E['allahumma-ainni-ala-dhikrika-wa-shukrika'] = dict(
    phrase=P(cut(he(3518, 'en'), 'O Allah, help me remember You, thank You, and excellently worship You'), cut(he(3518, 'ur'), 'اے اللہ! اپنا ذکر کرنے، شکر کرنے اور بہتر انداز میں اپنی عبادت کرنے میں میری مدد فرما۔')),
    hadith=P(cut(he(3518, 'en'), 'O Mu‘ādh, by Allah, I do love you," and he added: "I advise you, O Mu‘ādh, never fail to say after each prayer:'),
             cut(he(3518, 'ur'), 'اے معاذ! اللہ کی قسم، میں تم سے محبت رکھتا ہوں"۔ آگے فرمایا : "اے معاذ! میں تم کو وصیت کرتا ہوں کہ ہر نماز کے بعد یہ دعا پڑھنا ہرگز نہ چھوڑنا')))
E['allahummaghfir-li-warhamni-wahdini-wa-afini'] = dict(
    phrase=P(cut(hisn(130, 263), 'O Allah, forgive me, have mercy upon me, guide me, give me health and grant me sustenance.'), None),
    hadith=P(cut(ha('muslim', 2697, 'eng', '2697.03'), 'It is in these words (that there is supplication) which sums up for you (the good) of this world and that of the Hereafter'),
             cut(ha('muslim', 2697, 'urd', '2697.03'), 'یہ کلمات تمہارے لیے تمہاری دنیا اور آخرت دونوں جمع کر دیں گے')))
N['allahummaghfir-li-warhamni-wahdini-wa-afini'] = ['HadeethEnc #6112 has this du\'a without "wa \'afini", so it was not used. phrase.ur null: the Urdu of the matching narration (Muslim 2697, 2nd) leaves the words in Arabic, and the 1st narration lacks "wa \'afini". The virtue sentence comes from the 3rd narration (hadith-api).']
E['allahumma-inni-asalukal-jannata-wa-audhu'] = dict(
    phrase=P(cut(hisn(24, 61), 'O Allah, I ask You to grant me Paradise and I take refuge in You from the Fire.'),
             cut(ha('abudawud', 792, 'urd'), 'اے اللہ! میں تجھ سے جنت کا طالب ہوں اور جہنم سے تیری پناہ چاہتا ہوں')),
    hadith=P(cut(ha('tirmidhi', 2572, 'eng'), "Whoever asks Allah (s.w.t) Paradise three times, Paradise says: 'O Allah, admit him into Paradise', and whoever seeks refuge from the Fire three times, the Fire says: 'O Allah, save him from the Fire.'"),
             cut(ha('tirmidhi', 2572, 'urd'), 'جو اللہ تعالیٰ سے تین بار جنت مانگتا ہے تو جنت کہتی ہے: اے اللہ! اسے جنت میں داخل کر دے، اور جو تین مرتبہ جہنم سے پناہ مانگتا ہے تو جہنم کہتی ہے: اے اللہ اس کو جہنم سے نجات دے')))
N['allahumma-inni-asalukal-jannata-wa-audhu'] = ['Tirmidhi 2572 is not in HadeethEnc; hadith is hadith-api. phrase.en is Hisn al-Muslim.']

HEADERS = {
    'bukhari-6407': dict(
        en=cut(he(4177, 'en'), 'The example of the one who remembers his Lord and the one who does not remember His Lord is like the example of the living and the dead person.'),
        ur=cut(he(4177, 'ur'), 'اس شخص کی مثال جو اپنے رب کو یاد کرتا ہے اور جو اسے یاد نہیں کرتا، زندہ اور مردہ کی سی ہے')),
    'bukhari-5027': dict(
        en=cut(he(5913, 'en'), 'The best of you are those who learn the Qur’an and teach it.'),
        ur=cut(he(5913, 'ur'), 'تم میں سب سے بہتر شخص وہ ہے جو قرآن سیکھے اور اسے سکھائے')),
}

def main():
    dh = json.load(open(DHIKR))
    entries = {}
    stats = {k: 0 for k in ['phrase.en', 'phrase.ur', 'hadith.en', 'hadith.ur']}
    applicable_hadith = 0
    steps_total = steps_en = steps_ur = 0
    ids = []
    for s in dh['sections']:
        for e in s['dhikr']:
            ids.append(e['id'])
            spec = E.get(e['id'])
            if spec is None:
                raise SystemExit('missing spec for ' + e['id'])
            rec = {'phrase': spec['phrase'], 'hadith': spec['hadith']}
            if e.get('steps'):
                st = {}
                for step in e['steps']:
                    st[step['arabic']] = STEP(step['arabic'])
                    steps_total += 1; steps_en += 1; steps_ur += 1
                rec['steps'] = st
            if e['id'] in ALT: rec['alternatives'] = ALT[e['id']]
            if e['id'] in N: rec['notes'] = N[e['id']]
            entries[e['id']] = rec
            for l in ['en', 'ur']:
                if rec['phrase'][l]: stats['phrase.' + l] += 1
                if rec['hadith'] and rec['hadith'][l]: stats['hadith.' + l] += 1
            if rec['hadith'] is not None: applicable_hadith += 1
    extra = set(E) - set(ids)
    assert not extra, extra
    risk = sorted({i for i, r in entries.items() for f in ('phrase', 'hadith') if r[f] for l in ('en', 'ur') if r[f][l] and r[f][l].get('licensingRisk')})
    out = {'meta': {
        'generated': '2026-09-28',
        'hadeethEncTerms': 'HadeethEnc permits republishing only unmodified (no addition/deletion) with attribution to HadeethEnc.com; excerpts carry fullText with the unabridged translation.', 'rule': 'Every text is a verbatim, contiguous substring of the published translation at sourceUrl (verified by script). null = no published translation found in the allowed sources. Entries whose hadith is null are Quran-only (no hadith cited).',
        'sourcePriority': ['HadeethEnc.com (republish unmodified with attribution)', 'Hisn al-Muslim English (hisnmuslim.com; no Urdu edition available in machine-readable form)', 'fawazahmed0/hadith-api eng-/urd- editions (licensingRisk: true)', 'Quran: Saheeh International (en.sahih) / Fateh Muhammad Jalandhry (ur.jalandhry) via api.alquran.cloud'],
        'coverage': {**{k: f'{v}/{len(ids)}' for k, v in stats.items()}, 'hadithApplicable': applicable_hadith, 'steps': f'{steps_en}/{steps_total} en, {steps_ur}/{steps_total} ur'},
        'entriesWithLicensingRisk': risk},
        'entries': entries, 'headers': HEADERS}
    json.dump(out, open(OUT, 'w'), ensure_ascii=False, indent=2)
    print(json.dumps(out['meta']['coverage'], ensure_ascii=False), len(risk), 'risk entries')
    for i, r in entries.items():
        gaps = [f'{f}.{l}' for f in ('phrase', 'hadith') for l in ('en', 'ur') if r[f] is not None and not r[f][l]]
        if gaps: print('GAP', i, gaps)

if __name__ == '__main__':
    main()
