#!/usr/bin/env python3

import json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, "..", "data")
MUSHAF = os.path.join(DATA, "hafsData_v2-0.json")
MARKS = re.compile("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
STANDALONE = re.compile("^[\u06D6-\u06ED\u06DE\u06E9\uFC00-\uFDFF]+$")


def mushaf():
    rows = json.load(open(MUSHAF, encoding="utf-8"))
    return {(r["sura_no"], r["aya_no"]): r["aya_text"] for r in rows}


def words_and_mark(aya_text):
    return aya_text[:-1].rstrip("  "), aya_text[-1]


def tokens(text):
    return [t for t in re.split(r"[  ]+", text.strip()) if t]


def skeleton(word):
    w = MARKS.sub("", word)
    for a, b in (("ٱ", "ا"), ("أ", "ا"), ("إ", "ا"), ("آ", "ا"), ("ٲ", "ا"), ("ى", "ي"), ("ی", "ي"), ("ۥ", ""), ("ۦ", ""), ("ۧ", ""), ("ء", ""), ("ئ", "ي"), ("ؤ", "و"), ("ـ", "")):
        w = w.replace(a, b)
    return w.replace("اا", "ا")


def core(toks):
    return [i for i, t in enumerate(toks) if not STANDALONE.match(t)]


def span(reference):
    m = re.search(r"(\d+):(\d+)(?:\s*[-–]\s*(\d+))?", reference)
    return (int(m.group(1)), int(m.group(2)), int(m.group(3) or m.group(2))) if m else None


def passage(book, s, a, b):
    parts = []
    for n in range(a, b + 1):
        words, mark = words_and_mark(book[(s, n)])
        parts.append(words if n == b else f"{words} {mark}")
    return " ".join(parts)


def convert(excerpt, tanzil_full, mushaf_full):
    e_toks = [t for t in tokens(excerpt) if not STANDALONE.match(t)]
    t_toks = [t for t in tokens(tanzil_full) if not STANDALONE.match(t)]
    if not contains(t_toks, e_toks):
        return None, "excerpt is not verbatim Tanzil text"
    m_toks = tokens(mushaf_full)
    m_core = [i for i, t in enumerate(m_toks) if not STANDALONE.match(t)]
    m_sk = [skeleton(m_toks[i]) for i in m_core]
    e_sk = [skeleton(t) for t in e_toks]
    starts = [k for k in range(len(m_sk) - len(e_sk) + 1) if m_sk[k:k + len(e_sk)] == e_sk]
    if len(starts) != 1:
        return None, f"excerpt matches the official verse {len(starts)} times"
    k = starts[0]
    first = 0 if k == 0 else m_core[k]
    last = m_core[k + len(e_sk) - 1]
    return " ".join(m_toks[first:last + 1]), None


def contains(whole, part):
    return any(whole[i:i + len(part)] == part for i in range(len(whole) - len(part) + 1))


def entries(o, found):
    if isinstance(o, dict):
        if "arabic" in o and "surah" in o and "ayah" in o:
            found.append((o, f"{o['surah']}:{o['ayah']}"))
        elif "arabic" in o and isinstance(o.get("reference"), str) and span(o["reference"]):
            found.append((o, o["reference"]))
        for v in o.values():
            entries(v, found)
    elif isinstance(o, list):
        for v in o:
            entries(v, found)
    return found


def main(tanzil_path):
    book = mushaf()
    tanzil = {(s["number"], a["numberInSurah"]): a["text"].lstrip("﻿") for s in json.load(open(tanzil_path, encoding="utf-8"))["data"]["surahs"] for a in s["ayahs"]}
    failures, changed = [], 0
    for name in ("Duas", "Emotions", "Explore"):
        path = os.path.join(DATA, f"{name}.json")
        data = json.load(open(path, encoding="utf-8"))
        for entry, reference in entries(data, []):
            s, a, b = span(reference)
            mushaf_full = passage(book, s, a, b)
            if contains(tokens(mushaf_full), tokens(entry["arabic"])):
                continue
            tanzil_full = " ".join(tanzil[(s, n)] for n in range(a, b + 1))
            converted, problem = convert(entry["arabic"], tanzil_full, mushaf_full)
            if problem:
                failures.append((name, reference, problem, entry["arabic"]))
                continue
            entry["arabic"] = converted
            changed += 1
        open(path, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=1))
    for name, reference, problem, text in failures:
        print(f"{name} {reference}: {problem}\n  {text}")
    print(f"{changed} passages now in the King Fahd Complex text, {len(failures)} need a look")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1]))
