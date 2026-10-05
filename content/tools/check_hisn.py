#!/usr/bin/env python3

import html, json, os, re, sys, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, "cache", "hisn")
DATA = os.path.join(HERE, "..", "data", "HisnAlMuslim.json")
SITE = "https://www.hisnmuslim.com"
RN0X_COMMIT = "0405ee1797c2ccadfe82cd41845338d54978ccb9"
RN0X = f"https://raw.githubusercontent.com/rn0x/hisn_almuslim_json/{RN0X_COMMIT}/hisn_almuslim.json"

SITE_ONLY = {197: 86}
ARABIC_FROM_ARABIC_API = {114}
ENGLISH_IN_TRANSLITERATION = {133, 185}
HONORIFICS = (
    (re.compile(r"(?<=\S) t(?= )"), " رضي الله عنه"),
    (re.compile(r"(?<=\S) r(?= )"), " ﷺ"),
)
FOOTNOTES = {
    1: [2], 2: [3], 3: [5], 4: [6],
    19: [0, 1, 2, 3],
    20: [0, 1, 2, 3],
    23: [1, 2], 24: [3], 25: [4], 26: [5],
    74: [0, 1],
    75: [1], 76: [2], 77: [5], 78: [7], 79: [9], 80: [11], 81: [13], 82: [14], 83: [15], 84: [16], 85: [17], 86: [18],
    87: [19], 88: [20], 89: [23], 90: [25], 91: [26], 92: [27, 28], 93: [29], 94: [30], 95: [31], 96: [32], 97: [33], 98: [34],
    102: [4], 103: [5], 104: [7], 105: [8], 106: [9], 107: [10], 108: [11], 109: [12], 110: [13], 111: [15],
    114: [0, 1], 115: [4],
    134: [2], 135: [3],
    160: [0, 1], 161: [2],
    162: [0, 1],
    199: [0, 1],
}


def fetch(url, name):
    os.makedirs(CACHE, exist_ok=True)
    path = os.path.join(CACHE, name)
    if not os.path.exists(path):
        request = urllib.request.Request(url, headers={"User-Agent": "Muttaqi content check"})
        with urllib.request.urlopen(request, timeout=120) as response:
            body = response.read()
        open(path + ".part", "wb").write(body)
        os.replace(path + ".part", path)
    return open(path, encoding="utf-8-sig").read()


def api(lang, name):
    text = fetch(f"{SITE}/api/{lang}/{name}.json", f"api-{lang}-{name}.json")
    try:
        return json.loads(text, strict=False)
    except ValueError:
        repaired = re.sub(r'^(\s*\{\s*")([^"\n]*?)(\s*):(\s*\n)', r'\1\2"\3:\4', text, count=1)
        return json.loads(repaired, strict=False)


def records(lang, chapter):
    return {r["ID"]: r for r in list(api(lang, chapter).values())[0]}


def plain(fragment):
    return html.unescape(re.sub(r"<[^>]+>", "", fragment)).strip()


def site(lang, page, dua):
    text = fetch(f"{SITE}/i/{lang}/{page}", f"site-{lang}-{page}.html")
    m = re.search(r'data-audio="[^"]*/%d\.mp3"></a>\s*</span>(.*?)(?=<div class="thikr">|</div><!-- /\.site-post -->)' % dua, text, re.S)
    if not m:
        return None, None
    main, _, rest = m.group(1).partition('<div class="meaning">')
    meaning = re.search(r"<p>(.*?)</p>", rest, re.S)
    return plain(main), plain(meaning.group(1)) if meaning else None


def honorifics(text):
    for pattern, replacement in HONORIFICS:
        text = pattern.sub(replacement, text)
    return text


def rn0x():
    book = json.loads(fetch(RN0X, f"rn0x-{RN0X_COMMIT[:12]}.json"))
    return {n: book[k] for n, k in enumerate(list(book)[2:], start=1)}


def published(chapter_id, position, dua_id, notes):
    if dua_id in SITE_ONLY:
        page = SITE_ONLY[dua_id]
        transliteration, translation = site("en", page, dua_id)
        arabic, _ = site("ar", page, dua_id)
        expected = {
            "arabic": arabic,
            "transliteration": honorifics(transliteration) if transliteration is not None else None,
            "translation": honorifics(translation) if translation is not None else None,
        }
    else:
        source = records("en", chapter_id).get(dua_id)
        if source is None:
            return None
        expected = {"arabic": source.get("ARABIC_TEXT", source.get("Text", "")).strip(), "repeat": source["REPEAT"]}
        if dua_id in ENGLISH_IN_TRANSLITERATION:
            expected["transliteration"] = ""
            expected["translation"] = source["LANGUAGE_ARABIC_TRANSLATED_TEXT"].strip()
        else:
            expected["transliteration"] = source.get("LANGUAGE_ARABIC_TRANSLATED_TEXT", "").strip()
            expected["translation"] = source.get("TRANSLATED_TEXT", "").strip()
        if dua_id in ARABIC_FROM_ARABIC_API:
            expected["arabic"] = records("ar", chapter_id)[dua_id]["ARABIC_TEXT"].strip()
    footnotes = notes[chapter_id]["footnote"]
    expected["reference"] = "\n".join(footnotes[k].strip() for k in FOOTNOTES.get(dua_id, [position]))
    return expected


def first_difference(ours, theirs):
    ours, theirs = str(ours), str(theirs)
    k = next((n for n, (a, b) in enumerate(zip(ours, theirs)) if a != b), min(len(ours), len(theirs)))
    return k, ours[max(0, k - 30):k + 50], theirs[max(0, k - 30):k + 50]


def main():
    data = json.load(open(DATA, encoding="utf-8"))
    titles = {c["ID"]: c["TITLE"].strip() for c in api("en", "husn_en")["English"]}
    notes = rn0x()
    failures, duas, chapters = [], 0, 0
    for category in data["categories"]:
        for chapter in category["chapters"]:
            chapters += 1
            cid = chapter["id"]
            if chapter["title"] != titles.get(cid):
                failures.append((f"chapter {cid}", "title", chapter["title"], titles.get(cid)))
            for position, dua in enumerate(chapter["duas"]):
                duas += 1
                expected = published(cid, position, dua["id"], notes)
                if expected is None:
                    failures.append((f"dua {dua['id']}", "record", "present", "not in hisnmuslim.com chapter"))
                    continue
                for field, value in expected.items():
                    if dua.get(field) != value:
                        failures.append((f"dua {dua['id']}", field, dua.get(field), value))
    for where, field, ours, theirs in failures:
        k, a, b = first_difference(ours, theirs)
        print(f"DIFFERS FROM THE PUBLISHED TEXT: {where} {field} (first difference at character {k})")
        print(f"  ours:      {a!r}")
        print(f"  published: {b!r}\n")
    print(f"{duas} duas and {chapters} chapter titles checked against hisnmuslim.com and the book's footnotes (rn0x): {len(failures)} differ")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
