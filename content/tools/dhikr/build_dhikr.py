import json, re, sys
SRC = '/private/tmp/claude-501/-Users-vyro-Projects-IOS-Projects-Muttaqi/58111274-270b-439e-81ca-c9cc28a6407b/scratchpad/dhikr-candidates.json'
OUT = '/Users/vyro/Projects/IOS Projects/Muttaqi/Muttaqi/Resources/Data/Dhikr.json'
v = json.load(open(SRC))['verified']
PUBLISHED = json.load(open('/private/tmp/claude-501/-Users-vyro-Projects-IOS-Projects-Muttaqi/58111274-270b-439e-81ca-c9cc28a6407b/scratchpad/dhikr-published.json'))['entries']
# No published translation exists in the allowed sources, so the entry isn't shipped
DROPPED = {'subhanallahi-adada-ma-khalaq-wa-subhanallahi'}

def credit(source):
    """Short name of a published translation, from the source line recorded with it"""
    if source.startswith('HadeethEnc'): return 'HadeethEnc.com'
    if source.startswith('Hisn al-Muslim'): return 'Hisn al-Muslim (hisnmuslim.com)'
    if '—' in source:
        work, translator = [part.strip() for part in source.split('—', 1)]
        translator = translator.split(' (via')[0]
        if translator.startswith(('English: ', 'Urdu: ')):
            language, name = translator.split(': ', 1)
            if name.startswith('translator not named'):
                collection = work.rsplit(' ', 1)[0]
                return f'Published {language} translation of {collection}'
            return name
        return translator
    raise ValueError(source)

def published(entry_id, field, language):
    """The published text for a field, preferring the more accurate alternative where the research recorded one"""
    entry = PUBLISHED[entry_id]
    alternative = (entry.get('alternatives') or {}).get(f'{field}.{language}')
    value = alternative or (entry.get(field) or {}).get(language)
    if not value: return None
    # HadeethEnc may only be republished unmodified, so a hadith is shown in full
    text = value.get('fullText') if field == 'hadith' and value.get('fullText') else value['text']
    return text.strip(), credit(value['source'])

# Phrases used inside counted sets
STEP = {
    'سُبْحَانَ اللَّهِ': ('SubhanAllah', 'Glory be to Allah.'),
    'الْحَمْدُ لِلَّهِ': ('Alhamdulillah', 'All praise is for Allah.'),
    'اللَّهُ أَكْبَرُ': ('Allahu Akbar', 'Allah is the Greatest.'),
    'لَا إِلَهَ إِلَّا اللَّهُ': ('La ilaha illallah', 'There is no god but Allah.'),
    'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ':
        ("La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu, wa huwa 'ala kulli shay'in qadir",
         'There is no god but Allah alone, without partner; His is the dominion and His is the praise, and He has power over all things.'),
    'لَا إِلَهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ': ('La ilaha illallahu wallahu akbar', 'There is no god but Allah, and Allah is the Greatest.'),
    'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ': ('La ilaha illallahu wahdah', 'There is no god but Allah alone.'),
    'لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ': ('La ilaha illallahu wahdahu la sharika lah', 'There is no god but Allah alone, without partner.'),
    'لَا إِلَهَ إِلَّا اللَّهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ': ('La ilaha illallahu lahul-mulku wa lahul-hamd', 'There is no god but Allah; His is the dominion and His is the praise.'),
    'لَا إِلَهَ إِلَّا اللَّهُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ': ('La ilaha illallahu wa la hawla wa la quwwata illa billah', 'There is no god but Allah, and there is no might nor power except by Allah.'),
}

# Shown on the list row instead of the translation, for entries that are a set of phrases
TITLES = {
    15: 'Five declarations of oneness',
    54: 'After every prayer · 33, 33, 33 and 1',
    55: 'After every prayer · 33, 33 and 34',
    56: 'After every prayer · 10 each',
    57: 'After every prayer · 25 each',
    58: 'Before sleep · 33, 33 and 34',
    59: '100 each, as taught to Umm Hani',
}

# When or where the hadith places it; the candidate file's other notes are verification notes, not for readers
CONTEXT = {
    17: 'A Companion said it when opening the prayer.',
    18: 'Said in prayer, after rising from bowing.',
    19: 'Said on seeing something you dislike.',
    20: 'Said in bowing and prostration.',
    21: 'Said three times after the witr prayer.',
    25: 'Said at the end of a gathering.',
}

# Corrections: only undisputed virtues ship (Musnad Ahmad 8012 may be Ka'b's own words, per Ibn Rajab)
OVERRIDE = {
    3: {'virtue': 'Every takbir is an act of charity, as is every tasbih, tahmid and tahlil.',
        'reference': 'Sahih Muslim 1006', 'grade': 'Sahih (Muslim)'},
    44: {'virtue': 'The opening praise of al-Fatihah.', 'reference': 'Quran 1:2', 'grade': 'Quran'},
    12: {'virtue': 'Said 100 times a day: the reward of freeing ten slaves, 100 good deeds written, 100 sins erased, '
                   'and protection from Satan until evening. Said 10 times, it is like freeing four souls from the '
                   "descendants of Isma'il.",
         'reference': 'Sahih al-Bukhari 6403, 6404; Sahih Muslim 2691, 2693; Jami at-Tirmidhi 3585',
         'grade': 'Agreed upon (Bukhari & Muslim); Tirmidhi 3585 Hasan (al-Albani)'},
}

SECTIONS = [
    ('tasbih', 'Tasbih', 'Glorifying Allah', [4, 5, 0, 6, 7, 9, 8, 20, 21]),
    ('tahmid', 'Tahmid', 'Praising Allah', [1, 18, 19]),
    ('tahlil-takbir', 'Tahlil & Takbir', 'His oneness and greatness', [2, 12, 3, 14, 15, 22, 23, 24]),
    ('combined', 'Combined Words', 'The best words together', [10, 11, 27, 16, 17, 59]),
    ('istighfar', 'Istighfar', 'Seeking forgiveness', [32, 28, 29, 30, 26, 31, 33, 34, 25]),
    ('salawat', 'Salawat', 'Blessings on the Prophet ﷺ', [38, 35, 36, 37]),
    ('after-prayer-sleep', 'After Prayer & Sleep', 'Counted sets', [54, 55, 56, 57, 58]),
    ('quran', 'From the Quran', 'Remembrance in Allah’s words', [39, 40, 41, 42, 43, 44, 45, 46, 47, 48]),
    ('supplications', 'Supplications', 'Short duas to repeat', [49, 50, 51, 52, 53]),
]

used = [i for s in SECTIONS for i in s[3]]
assert sorted(used + [13]) == list(range(len(v))), sorted(set(range(len(v))) - set(used))
assert len(used) == len(set(used))

ids = set()
def slug(text):
    base = re.sub(r'[^a-z0-9]+', '-', text.lower().replace("'", '')).strip('-')
    base = '-'.join(base.split('-')[:6])
    s, n = base, 2
    while s in ids:
        s, n = f'{base}-{n}', n + 1
    ids.add(s)
    return s

sections = []
for sid, title, subtitle, order in SECTIONS:
    items = []
    for i in order:
        e = {**v[i], **OVERRIDE.get(i, {})}
        entry_id = slug(TITLES.get(i) or e['transliteration'])
        if entry_id in DROPPED: continue
        item = {'id': entry_id}
        if i in TITLES: item['title'] = TITLES[i]
        # A set lists its phrases; the counts are in its title, and the Quran font would draw digits as ayah markers
        arabic = '، '.join(step['arabic'] for step in e['sequence']) if e.get('sequence') else e['arabic']
        credits = {'en': [], 'ur': []}
        def take(field, language):
            found = published(entry_id, field, language)
            if not found: return None
            text, name = found
            if name not in credits[language]: credits[language].append(name)
            return text
        item.update({'arabic': arabic, 'transliteration': e['transliteration']})
        translation = {lang: t for lang in ('en', 'ur') if (t := take('phrase', lang))}
        if translation: item['translation'] = translation
        if e.get('sequence'):
            steps_published = PUBLISHED[entry_id]['steps']
            item['steps'] = []
            for step in e['sequence']:
                texts = steps_published[step['arabic']]
                step_translation = {}
                for lang in ('en', 'ur'):
                    if texts.get(lang):
                        step_translation[lang] = texts[lang]['text'].strip()
                        name = credit(texts[lang]['source'])
                        if name not in credits[lang]: credits[lang].append(name)
                item['steps'].append({'arabic': step['arabic'], 'transliteration': STEP[step['arabic']][0],
                                      'translation': step_translation, 'count': step['count']})
        elif e.get('count'):
            item['count'] = e['count']
        hadith = {lang: t for lang in ('en', 'ur') if (t := take('hadith', lang))}
        if hadith: item['hadith'] = hadith
        item['credit'] = {lang: ', '.join(names) for lang, names in credits.items() if names}
        item['reference'] = e['reference']
        item['grade'] = e['grade']
        items.append(item)
    sections.append({'id': sid, 'title': title, 'subtitle': subtitle, 'dhikr': items})

out = {
    'source': ('Compiled for Muttaqi from the Quran, Sahih al-Bukhari, Sahih Muslim and the Sunan, keeping only '
               'Quranic text and hadith graded sahih or hasan (grades by al-Albani and others, as noted per entry). '
               'Arabic hadith text from github.com/fawazahmed0/hadith-api; Quran text from Tanzil (tanzil.net). '
               'Translations are word for word from published ones, credited per entry: HadeethEnc.com, Hisn al-Muslim '
               '(hisnmuslim.com), Saheeh International, Fateh Muhammad Jalandhry and Muhammad Junagarhi for the Quran, and '
               'the published translations of the hadith collections. Transliterations written for this app.'),
    'sections': sections,
}
json.dump(out, open(OUT, 'w'), ensure_ascii=False, indent=1)
print('sections', len(sections), 'dhikr', sum(len(s['dhikr']) for s in sections))
for s in sections: print(' ', s['title'], len(s['dhikr']))
