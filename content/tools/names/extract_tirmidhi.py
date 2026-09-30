# -*- coding: utf-8 -*-
import json, re, html, unicodedata
D = '/private/tmp/claude-501/-Users-vyro-Projects-IOS-Projects-Muttaqi/58111274-270b-439e-81ca-c9cc28a6407b/scratchpad/'

def page_text(path):
    s = open(D + path, encoding='utf-8', errors='ignore').read()
    t = re.sub(r'<script.*?</script>|<style.*?</style>', '', s, flags=re.S)
    t = html.unescape(re.sub(r'<[^>]+>', '\n', t))
    return re.sub(r'\s+', ' ', t)

# ---------- sunnah.com (Arabic + Darussalam English) ----------
st = page_text('sunnah_t3507_2026.html')

# Arabic (located by letters only, output uses the source's own strings)
def bare(w):
    w = re.sub(r'[\u064B-\u0652\u0670\u0640\u200f\u200e]', '', w)
    for a, b in (('أ', 'ا'), ('إ', 'ا'), ('آ', 'ا'), ('ى', 'ي'), ('ؤ', 'ء'), ('ة', 'ه')):
        w = w.replace(a, b)
    return w
words = st.split(' ')
bw = [bare(w) for w in words]
start = None
for i in range(len(bw) - 7):
    if bw[i:i+7] == ['هو', 'الله', 'الذي', 'لا', 'اله', 'الا', 'هو']:
        start = i
        break
assert start is not None
toks = []
i = start + 7
while True:
    w = words[i]
    if bare(w).startswith('قال') or '"' in w or '\u201d' in w:
        break
    toks.append(w)
    i += 1
# end token may carry the closing mark; stop at the token 'الصبور'
endi = [bare(t) for t in toks].index('الصبور')
toks = toks[:endi + 1]
ar_names = [words[start + 1]]  # اللَّهُ as printed
j = 0
while j < len(toks):
    b = bare(toks[j])
    if b == 'مالك' and bare(toks[j+1]) == 'الملك':
        ar_names.append(toks[j] + ' ' + toks[j+1]); j += 2
    elif b == 'ذو' and bare(toks[j+1]) == 'الجلال':
        ar_names.append(' '.join(toks[j:j+3])); j += 3
    else:
        ar_names.append(toks[j]); j += 1
arabic_block = ' '.join(words[start:start + 7] + toks)

# English
ei = st.find('He is Allah, the one whom')
ej = st.find('”', ei)  # closing quote
eng_block = st[ei:ej].strip()
assert eng_block.endswith('.'), eng_block[-20:]
eng_body = eng_block[:-1]  # drop the sentence-final period only
segs = re.split(r'(?<=\)),\s+', eng_body)
en = []
for s in segs:
    m = re.match(r'^(.*)\s\(([^()]+)\)$', s)
    assert m, s
    meaning, translit = m.group(1).strip(), m.group(2).strip()
    en.append((meaning, translit))

# ---------- islamicurdubooks (al-Faryiwa'i Urdu) ----------
ut = page_text('iub_t3507.html')
ui = ut.find('ڈاکٹر عبدالرحمٰن فریوائی')
ui = ut.find('«الله»', ui)
uj = ut.find('امام ترمذی کہتے ہیں', ui)
ur_block = ut[ui:uj]
# split into entries at each «...»
parts = re.split(r'(«[^»]+»)', ur_block)
ur = []
k = 1
while k < len(parts):
    head = parts[k].strip('«»').strip()
    body = parts[k + 1] if k + 1 < len(parts) else ''
    k += 2
    body = body.strip()
    meaning = None
    m = re.match(r'^”\s*(.*?)\s*[“‘]\s*[،۔]?\s*$', body)
    if m:
        meaning = m.group(1).strip()
    elif head == 'الله':
        # printed without quotation marks
        meaning = re.sub(r'\s*،\s*$', '', body).strip()
    elif body in ('،', '', '۔'):
        meaning = None
    else:
        raise SystemExit('unparsed Urdu entry: %r %r' % (head, body))
    ur.append((head, meaning))

# ---------- alignment checks ----------
def norm(s):
    s = re.sub(r'[ً-ْٰـ]', '', s)
    for a, b in (('أ', 'ا'), ('إ', 'ا'), ('آ', 'ا'), ('ى', 'ي'), ('ؤ', 'ء'), ('ة', 'ه')):
        s = s.replace(a, b)
    return s.replace(' ', '')

print('arabic', len(ar_names), 'english', len(en), 'urdu', len(ur))
assert len(ar_names) == 99 and len(en) == 99 and len(ur) == 99
mism = [(i + 1, ar_names[i], ur[i][0]) for i in range(99) if norm(ar_names[i]) != norm(ur[i][0])]
print('arabic vs urdu headword mismatches:', mism)
assert not mism

names = []
for i in range(99):
    names.append({
        'number': i + 1,
        'arabic': ar_names[i],
        'transliteration': en[i][1],
        'meaning': {'en': en[i][0], 'ur': ur[i][1]},
    })

# ---------- hadith (HadeethEnc 64673) ----------
he_en = json.load(open(D + 'hadeethenc_64673_en.json', encoding='utf-8'))
he_ur = json.load(open(D + 'hadeethenc_64673_ur.json', encoding='utf-8'))
q_en = re.search(r'"(.+)"', he_en['hadeeth']).group(1)
q_ur = re.search(r'"(.+)"', he_ur['hadeeth']).group(1)

out = {
    'source': {
        'arabic': 'Jami` at-Tirmidhi 3507, Arabic text as printed on sunnah.com (https://sunnah.com/tirmidhi:3507; read from the web.archive.org snapshot of 2026-03-12, identical to the 2025-12-08 snapshot)',
        'en': 'Jami` at-Tirmidhi 3507, English translation by Darussalam as published on sunnah.com (English translation ref: Vol. 6, Book 45, Hadith 3507)',
        'ur': "Sunan at-Tirmidhi 3507, Urdu translation by Dr. Abdur-Rahman al-Faryiwa'i (Majlis Ilmi Dar al-Da'wah, New Delhi) as published on islamicurdubooks.com (https://www.islamicurdubooks.com/hadith/hadith-.php?bookid=6&hadith_number=3507)",
        'grading': "At-Tirmidhi called the hadith gharib, and al-Albani graded it sahih without the list but da'if with the listing of the names (Mishkat 2288; Da'if al-Jami' 1945); Darussalam grades it da'if.",
    },
    'hadith': {
        'en': {
            'text': q_en,
            'fullText': he_en['hadeeth'],
            'source': 'HadeethEnc.com, hadith #64673 (English), translation version v1.25.0; attribution: ' + he_en['attribution'] + '; grade: ' + he_en['grade'],
            'sourceUrl': 'https://hadeethenc.com/en/browse/hadith/64673',
        },
        'ur': {
            'text': q_ur,
            'fullText': he_ur['hadeeth'],
            'source': 'HadeethEnc.com، حدیث #64673 (اردو), translation version v1.36.0; ' + he_ur['attribution'] + ' - ' + he_ur['grade'],
            'sourceUrl': 'https://hadeethenc.com/ur/browse/hadith/64673',
        },
    },
    'names': names,
}
json.dump(out, open(D + 'names-tirmidhi.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=2)

print('en meanings non-null:', sum(1 for n in names if n['meaning']['en']))
print('ur meanings non-null:', sum(1 for n in names if n['meaning']['ur']))
print('translit non-null:', sum(1 for n in names if n['transliteration']))
print('ur nulls:', [(n['number'], n['arabic']) for n in names if not n['meaning']['ur']])
for n in names:
    print(n['number'], n['arabic'], '|', n['transliteration'], '|', n['meaning']['en'], '|', n['meaning']['ur'])
print(json.dumps(out['hadith'], ensure_ascii=False, indent=1))
