#!/usr/bin/env python3

import hashlib, json, os, re, sys, unicodedata, urllib.request

from mushaf_text import contains, mushaf, tokens
from mushaf_text import passage as mushaf_passage

HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, "cache", "reference")
DATA = os.path.join(HERE, "..", "data")
MUSHAF_FILE = os.path.join(DATA, "hafsData_v2-0.json")
MUSHAF_SHA256 = "d2960b3217962e7e4252abdcece67bea3d6b48271e4cd3af45bbbb2dd5c872ca"
COPIED_50_21 = "Wa jaaa'at kullu nafsim ma'ahaa saaa'iqunw wa shaheed"
CORRECTED_50_19 = "Wajaat sakratu almawti bialhaqqi thalika ma kunta minhu taheedu"
MIRRORS = {
    "qurani.ai": "https://api.qurani.ai/gw/qh/v1/quran/{}",
    "alquran.cloud": "https://api.alquran.cloud/v1/quran/{}",
}


def edition(mirror, name):
    os.makedirs(CACHE, exist_ok=True)
    path = os.path.join(CACHE, f"{mirror}-{name}.json")
    if not os.path.exists(path):
        request = urllib.request.Request(MIRRORS[mirror].format(name), headers={"User-Agent": "Muttaqi content check"})
        with urllib.request.urlopen(request, timeout=120) as response:
            open(path, "wb").write(response.read())
    surahs = json.load(open(path, encoding="utf-8"))["data"]["surahs"]
    book = {(s["number"], a["numberInSurah"]): a["text"].lstrip("\ufeff") for s in surahs for a in s["ayahs"]}
    words = len(book[(1, 1)].split())
    for (s, a), text in book.items():
        opening = text.split()[:words]
        if a == 1 and s not in (1, 9) and len(text.split()) > words and bare(" ".join(opening)) == bare(book[(1, 1)]):
            book[(s, a)] = text.split(None, words)[words]
    return book


def bare(text):
    return re.sub(r"[\u064B-\u065F\u0670\u06D6-\u06ED]", "", text)


def span(reference):
    m = re.search(r"(\d+):(\d+)(?:\s*[-–]\s*(\d+))?", reference)
    return (int(m.group(1)), int(m.group(2)), int(m.group(3) or m.group(2))) if m else None


def verses(o, found):
    if isinstance(o, dict):
        if "arabic" in o and "surah" in o and "ayah" in o:
            found.append((f"{o['surah']}:{o['ayah']}", o["arabic"]))
        elif "arabic" in o and isinstance(o.get("reference"), str) and span(o["reference"]):
            found.append((o["reference"], o["arabic"]))
        for v in o.values():
            verses(v, found)
    elif isinstance(o, list):
        for v in o:
            verses(v, found)
    return found


def passage(book, reference):
    s, a, b = span(reference)
    if any((s, n) not in book for n in range(a, b + 1)):
        return None
    return " ".join(book[(s, n)] for n in range(a, b + 1))


def contained(text, expected, normalize):
    clean = normalize(re.sub(r"\s+", " ", text).strip())
    return expected is not None and f" {clean} " in f" {normalize(expected)} "


def check(label, texts, books, normalize):
    failures, disagreements = [], []
    for reference, arabic in texts:
        expected = {mirror: passage(book, reference) for mirror, book in books.items()}
        matches = {mirror: contained(arabic, e, normalize) for mirror, e in expected.items()}
        if not any(matches.values()):
            failures.append((label, reference, arabic, expected))
        elif not all(matches.values()):
            disagreements.append((label, reference, arabic, expected))
    return failures, disagreements


def report(title, rows):
    for label, reference, arabic, expected in rows:
        print(f"{title}: {label} {reference}\n  ours: {arabic}")
        for mirror, e in expected.items():
            print(f"  {mirror}: {e}")
        print()


def check_mushaf_file():
    digest = hashlib.sha256(open(MUSHAF_FILE, "rb").read()).hexdigest()
    rows = json.load(open(MUSHAF_FILE, encoding="utf-8"))
    problems = []
    if digest != MUSHAF_SHA256:
        problems.append(f"hafsData_v2-0.json is not the file the King Fahd Complex published (sha256 {digest})")
    if len(rows) != 6236 or len({r["sura_no"] for r in rows}) != 114:
        problems.append(f"hafsData_v2-0.json has {len(rows)} ayahs")
    return problems


def check_mushaf_passages(texts, book):
    failures = []
    for reference, arabic in texts:
        s, a, b = span(reference)
        if any((s, n) not in book for n in range(a, b + 1)):
            failures.append(("Mushaf", reference, arabic, {"King Fahd Complex": None}))
            continue
        expected = mushaf_passage(book, s, a, b)
        if not contains(tokens(expected), tokens(arabic)):
            failures.append(("Mushaf", reference, arabic, {"King Fahd Complex": expected}))
    return failures


def translated(texts, label, books):
    failures = []
    for reference, text in texts:
        if not text:
            continue
        expected = {mirror: passage(book, reference) for mirror, book in books.items()}
        if not all(e is not None and text in e for e in expected.values()):
            failures.append((label, reference, text, expected))
    return failures


def translations(o, found):
    if isinstance(o, dict):
        reference = f"{o['surah']}:{o['ayah']}" if "surah" in o and "ayah" in o else o.get("reference")
        if isinstance(reference, str) and span(reference) and ("arabic" in o or "translation" in o):
            mapping = o.get("translations") or o.get("translation") or {}
            for key, edition_name in (("en", "en.sahih"), ("ur", "ur.jalandhry")):
                if isinstance(mapping, dict) and key in mapping:
                    found.append((edition_name, reference, mapping[key]))
            if isinstance(o.get("transliteration"), str):
                found.append(("en.transliteration", reference, o["transliteration"]))
        for v in o.values():
            translations(v, found)
    elif isinstance(o, list):
        for v in o:
            translations(v, found)
    return found


def check_translations():
    found = []
    for name in ("Duas", "Emotions", "Explore"):
        found += translations(json.load(open(os.path.join(DATA, f"{name}.json"), encoding="utf-8")), [])
    failures = []
    for edition_name in ("en.sahih", "ur.jalandhry", "en.transliteration"):
        books = {m: edition(m, edition_name) for m in MIRRORS}
        if edition_name == "en.transliteration":
            books = {m: {**b, (50, 19): CORRECTED_50_19} if b[(50, 19)] == COPIED_50_21 else b for m, b in books.items()}
        failures += translated([(r, t) for e, r, t in found if e == edition_name], edition_name, books)
    return len(found), failures


def main():
    nfc = lambda t: unicodedata.normalize("NFC", t)
    simple = {m: edition(m, "quran-simple") for m in MIRRORS}
    file_problems = check_mushaf_file()
    for problem in file_problems:
        print(problem)
    book = mushaf()
    failures, disagreements, total = [], [], 0
    for name in ("Duas", "Emotions", "Explore"):
        texts = verses(json.load(open(os.path.join(DATA, f"{name}.json"), encoding="utf-8")), [])
        total += len(texts)
        failures += check_mushaf_passages(texts, book)
    dhikr = json.load(open(os.path.join(DATA, "Dhikr.json"), encoding="utf-8"))
    quranic = [(x["reference"], x["arabic"]) for s in dhikr["sections"] for x in s["dhikr"] if x["reference"].startswith("Quran")]
    total += len(quranic)
    f, d = check("Dhikr", quranic, simple, nfc)
    failures += f
    disagreements += d
    report("DIFFERS FROM THE PUBLISHED TEXT", failures)
    report("MIRRORS DISAGREE, CHECK BY HAND", disagreements)
    print(f"{total} Quran passages checked letter by letter: {len(failures)} differ, {len(disagreements)} need a manual look")
    count, wrong = check_translations()
    report("TRANSLATION DIFFERS FROM THE PUBLISHED TEXT", wrong)
    print(f"{count} translations and transliterations checked character for character: {len(wrong)} differ")
    return 1 if failures or file_problems or wrong else 0


if __name__ == "__main__":
    sys.exit(main())
