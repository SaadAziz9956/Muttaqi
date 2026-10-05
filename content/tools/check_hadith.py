#!/usr/bin/env python3

import json, os, sys, time, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(HERE, "cache", "hadeethenc")
DATA = os.path.join(HERE, "..", "data")
API = "https://hadeethenc.com/api/v1/hadeeths/one/?language={}&id={}"
SYMBOL_FONT_SPACE = " "


def hadeeth(language, id):
    os.makedirs(CACHE, exist_ok=True)
    path = os.path.join(CACHE, f"{language}-{id}.json")
    if not os.path.exists(path):
        request = urllib.request.Request(API.format(language, id), headers={"User-Agent": "Muttaqi content check"})
        for attempt in range(5):
            try:
                with urllib.request.urlopen(request, timeout=60) as response:
                    open(path, "wb").write(response.read())
                break
            except Exception:
                if attempt == 4:
                    raise
                time.sleep(2 + attempt * 2)
    return json.load(open(path, encoding="utf-8"))


def entries(o, found):
    if isinstance(o, dict):
        if o.get("source") == "HadeethEnc.com" and "id" in o:
            found.append(o)
        for v in o.values():
            entries(v, found)
    elif isinstance(o, list):
        for v in o:
            entries(v, found)
    return found


def published(language, id):
    record = hadeeth(language, id)
    return {
        "arabic": record["hadeeth_ar"].replace(SYMBOL_FONT_SPACE, " "),
        "translation": record["hadeeth"],
        "attribution": record["attribution"],
        "grade": record["grade"],
    }


def main():
    failures, total = [], 0
    for name in ("Explore", "Emotions"):
        for entry in entries(json.load(open(os.path.join(DATA, f"{name}.json"), encoding="utf-8")), []):
            total += 1
            for language in ("en", "ur"):
                source = published(language, entry["id"])
                for field in ("arabic", "translation", "attribution", "grade"):
                    ours = entry[field].get(language)
                    if ours != source[field]:
                        failures.append((name, entry["id"], f"{field}.{language}", ours, source[field]))
    for name, id, field, ours, source in failures:
        print(f"DIFFERS FROM HADEETHENC: {name} #{id} {field}\n  ours: {ours}\n  HadeethEnc: {source}\n")
    print(f"{total} hadith checked character for character against HadeethEnc: {len(failures)} fields differ")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
