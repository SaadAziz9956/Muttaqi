#!/usr/bin/env python3
"""Checks an Explore group file against the published sources. Usage: python3 check.py faith.json"""
import json, re, sys
from tools import quran, corpus, usable, hisn

def verse_texts(ref, q):
    m = re.fullmatch(r"(\d+):(\d+)(?:-(\d+))?", ref)
    if not m: return None
    s, a, b = int(m[1]), int(m[2]), int(m[3] or m[2])
    if b < a or b - a > 4: return None
    refs = [f"{s}:{n}" for n in range(a, b + 1)]
    if any(r not in q["ar"] for r in refs): return None
    return {l: " ".join(q[l][r] for r in refs) for l in ("ar", "en", "ur")}

def main(path):
    data = json.load(open(path))
    q, c = quran(), corpus()
    hisn_ids = {int(d["id"]): d for _, d in hisn()}
    problems = []
    for t in data["topics"]:
        tid = t.get("id")
        for k in ("id", "title", "keywords", "quran", "hadith", "duas"):
            if k not in t: problems.append(f"{tid}: missing {k}")
        for v in t.get("quran", []):
            texts = verse_texts(v.get("ref", ""), q)
            if not texts: problems.append(f"{tid}: bad Quran ref {v.get('ref')}"); continue
            cut = v.get("cut")
            if cut:
                for l in ("ar", "en", "ur"):
                    if not cut.get(l) or cut[l] not in texts[l]:
                        problems.append(f"{tid}: {v['ref']} cut[{l}] is not an exact substring of the published text")
        for h in t.get("hadith", []):
            r = c.get(str(h.get("id")))
            if not r: problems.append(f"{tid}: hadith {h.get('id')} not in the English+Urdu corpus")
            elif not usable(r): problems.append(f"{tid}: hadith {h['id']} not usable ({r['grade_en']})")
        for d in t.get("duas", []):
            e = hisn_ids.get(int(d.get("hisnId", -1)))
            if not e: problems.append(f"{tid}: Hisn al-Muslim {d.get('hisnId')} doesn't exist")
            elif not e.get("translationUrdu"): problems.append(f"{tid}: Hisn al-Muslim {d['hisnId']} has no Urdu")
        nq, nh = len(t.get("quran", [])), len(t.get("hadith", []))
        if nq < 3: problems.append(f"{tid}: only {nq} Quran entries (aim for 4-6)")
        if nh < 1: problems.append(f"{tid}: no hadith")
    for p in problems: print("PROBLEM:", p)
    print("OK" if not problems else f"{len(problems)} problems")

if __name__ == "__main__":
    main(sys.argv[1])
