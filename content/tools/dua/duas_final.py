import json, unicodedata, re
E = {e: json.load(open(f"editions/{e}.json"))["data"]["surahs"] for e in
     ["quran-uthmani", "en.transliteration", "en.sahih", "ur.jalandhry"]}
def text(ed, s, a): return E[ed][s-1]["ayahs"][a-1]["text"]
def bare(w): return "".join(c for c in unicodedata.normalize("NFD", w) if unicodedata.category(c) != "Mn" and c != "ـ")

DEFAULT = dict(ar=1, tl=1, en=1, ur=1)
WHOLE = dict(ar=0, tl=0, en=0, ur=0)
DUAS = {
    (2,127): {}, (2,128): {}, (2,201): {}, (2,250): {}, (2,286): {}, (3,8): WHOLE, (3,16): {},
    (3,38): dict(ar=2, tl=2, en=1, ur=2), (3,53): WHOLE, (3,147): {}, (3,191): {}, (3,192): WHOLE,
    (3,193): WHOLE, (3,194): WHOLE, (5,83): {}, (7,23): {}, (7,47): {}, (7,126): dict(ar=2, tl=2, en=1, ur=2),
    (10,85): {}, (14,40): WHOLE, (14,41): WHOLE, (17,24): {}, (18,10): {}, (20,114): {},
    (21,89): dict(ar=2, tl=2, en=1, ur=2), (23,109): {}, (23,118): {}, (25,65): {}, (25,74): {},
    (26,83): WHOLE, (27,19): {}, (28,24): {}, (46,15): {}, (59,10): {}, (60,4): {}, (60,5): WHOLE,
    (66,8): dict(ar=2, tl=2, en=1, ur=1), (71,28): WHOLE,
}
def cut_words(t, n, is_start):
    if n == 0: return t
    words = t.split(" ")
    idx = [i for i, w in enumerate(words) if is_start(w)]
    return " ".join(words[idx[n-1]:])
def cut_marker(t, n, pattern):
    if n == 0: return t
    return t[[m.start() for m in re.finditer(pattern, t)][n-1]:]
def clean_en(t):
    t = re.sub(r'^\[[^\]]*\],?\s*', '', t)
    t = t.strip().strip('"“”').strip()
    t = re.sub(r"[,;]$", ".", t.rstrip("'").rstrip())
    return t if t[-1] in ".!?" else t + "."
def cap(t): return t[0].upper() + t[1:]

out = []
for (s, a), o in DUAS.items():
    o = {**DEFAULT, **o}
    out.append({
        "surah": s, "ayah": a,
        "arabic": cut_words(text("quran-uthmani", s, a), o["ar"], lambda w: bare(w).startswith("رب")),
        "transliteration": cap(cut_words(text("en.transliteration", s, a), o["tl"],
                                         lambda w: re.sub(r"[^a-z]", "", w.lower()).startswith("rabb"))),
        "translations": {
            "en": clean_en(cut_marker(text("en.sahih", s, a), o["en"], r"(Our|My) Lord")),
            "ur": cut_marker(text("ur.jalandhry", s, a), o["ur"], r"(اے )?(ہمارے |میرے )?پروردگار").strip(),
        },
    })
json.dump(out, open("Duas.json", "w"), ensure_ascii=False, indent=2)
print(len(out), "duas")
for d in out:
    print(f"\n{d['surah']}:{d['ayah']}\n  {d['arabic']}\n  {d['transliteration']}\n  {d['translations']['en']}\n  {d['translations']['ur']}")
