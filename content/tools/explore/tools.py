#!/usr/bin/env python3
"""Local lookup tools for curating Explore content. Everything reads published texts from local copies:
Quran (quran-uthmani, en.sahih, ur.jalandhry from api.alquran.cloud), HadeethEnc (English + Urdu, scholar-reviewed),
and the app's bundled Hisn al-Muslim.

  python3 tools.py ayah 2:274 3:92          Arabic / English / Urdu of each ayah
  python3 tools.py qsearch charity spend    ayahs whose Saheeh English contains every word (case-insensitive)
  python3 tools.py qsearch-ur زکوٰة         ayahs whose Jalandhry Urdu contains every word
  python3 tools.py hsearch wine intoxicant  hadith whose English text/title contains every word
  python3 tools.py hsearch-ar الخمر         hadith whose Arabic contains every word (diacritics ignored)
  python3 tools.py hadith 5913 4709         full Arabic / English / Urdu / attribution / grade
  python3 tools.py cats                     HadeethEnc categories with ids
  python3 tools.py cat 94                   hadith in a category (and its subcategories)
  python3 tools.py hisn sleep               Hisn al-Muslim duas whose English/chapter contains every word
  python3 tools.py hisnid 99                one Hisn al-Muslim dua in full

A hadith is usable only if `usable: yes` (graded sahih or hasan and has both English and Urdu).
"""
import json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
# Downloaded sources (not committed): cache/editions/*.json from api.alquran.cloud and cache/he/{ar,en,ur}/*.json from
# the HadeethEnc API; see ../README.md
SCRATCH = os.path.join(os.path.dirname(HERE), "cache")
REPO = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
PROJECT = os.path.join(REPO, "content", "data")

UNGRADED = ("No judgment", "left no comment")

def _edition(name):
    d = json.load(open(os.path.join(SCRATCH, "editions", f"{name}.json")))["data"]["surahs"]
    return {f"{s['number']}:{a['numberInSurah']}": a["text"] for s in d for a in s["ayahs"]}

_q = {}
def quran():
    if not _q:
        _q["ar"] = _edition("quran-uthmani")
        _q["en"] = _edition("en.sahih")
        _q["ur"] = _edition("ur.jalandhry")
    return _q

def corpus():
    return json.load(open(os.path.join(SCRATCH, "he_corpus.json")))

def usable(r):
    g = r.get("grade_en", "")
    return bool(r.get("text_en") and r.get("text_ur")) and not any(u in g for u in UNGRADED) \
        and not re.search(r"weak|da.?if|fabricat", g, re.I)

def strip_ar(t):
    return re.sub(r"[ً-ٰٟۖ-ۭـ]", "", t).replace("ٱ", "ا").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")

def hisn():
    book = json.load(open(os.path.join(PROJECT, "HisnAlMuslim.json")))
    for cat in book["categories"]:
        for ch in cat["chapters"]:
            for d in ch["duas"]:
                yield ch, d

def main(cmd, args):
    if cmd == "ayah":
        q = quran()
        for ref in args:
            print(f"== {ref}\nAR: {q['ar'].get(ref)}\nEN: {q['en'].get(ref)}\nUR: {q['ur'].get(ref)}\n")
    elif cmd in ("qsearch", "qsearch-ur"):
        q = quran()
        lang = "en" if cmd == "qsearch" else "ur"
        words = [w.lower() for w in args]
        hits = [r for r, t in q[lang].items() if all(w in t.lower() for w in words)]
        print(len(hits), "ayahs")
        for r in hits[:60]:
            print(f"{r}: {q[lang][r][:220]}")
    elif cmd in ("hsearch", "hsearch-ar"):
        c = corpus()
        if cmd == "hsearch":
            words = [w.lower() for w in args]
            hits = [r for r in c.values() if all(w in (r["text_en"] + " " + r.get("title_en", "")).lower() for w in words)]
        else:
            words = [strip_ar(w) for w in args]
            hits = [r for r in c.values() if all(w in strip_ar(r.get("arabic", "")) for w in words)]
        print(len(hits), "hadith")
        for r in hits[:40]:
            print(f"[{r['id']}] usable: {'yes' if usable(r) else 'NO'} | {r['grade_en']} | {r['attribution_en']}\n   {r['text_en'][:260]}")
    elif cmd == "hadith":
        c = corpus()
        for i in args:
            r = c.get(i)
            if not r: print(f"== {i}: not in corpus (no English+Urdu)"); continue
            print(f"== {i}  usable: {'yes' if usable(r) else 'NO'}\nGRADE: {r['grade_en']} / {r['grade_ur']}\n"
                  f"ATTRIBUTION: {r['attribution_en']} / {r['attribution_ur']}\nAR: {r['arabic']}\nEN: {r['text_en']}\nUR: {r['text_ur']}\n")
    elif cmd == "cats":
        for x in json.load(open(os.path.join(SCRATCH, "he", "cats_en.json"))):
            print(x["id"], "parent", x["parent_id"], "|", x["title"], f"({x['hadeeths_count']})")
    elif cmd == "cat":
        cats = json.load(open(os.path.join(SCRATCH, "he", "cats_en.json")))
        want = set(args)
        changed = True
        while changed:
            changed = False
            for x in cats:
                if x["parent_id"] in want and x["id"] not in want:
                    want.add(x["id"]); changed = True
        c = corpus()
        hits = [r for r in c.values() if set(re.findall(r"\d+", str(r.get("categories")))) & want]
        print(len(hits), "hadith")
        for r in hits:
            print(f"[{r['id']}] usable: {'yes' if usable(r) else 'NO'} | {r['grade_en']} | {r.get('title_en','')[:150]}")
    elif cmd == "hisn":
        words = [w.lower() for w in args]
        for ch, d in hisn():
            hay = (ch["title"] + " " + (d.get("translation") or "")).lower()
            if all(w in hay for w in words):
                print(f"[{d['id']}] ({ch['title']}) ur: {'yes' if d.get('translationUrdu') else 'NO'}\n   {d['arabic'][:120]}\n   {(d.get('translation') or '')[:200]}")
    elif cmd == "hisnid":
        want = {int(a) for a in args}
        for ch, d in hisn():
            if int(d["id"]) in want:
                print(f"== {d['id']} ({ch['title']})\nAR: {d['arabic']}\nEN: {d.get('translation')}\nUR: {d.get('translationUrdu')}\nSOURCE: {d.get('reference') or d.get('source')}\n")
    else:
        print(__doc__)

if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "", sys.argv[2:])
