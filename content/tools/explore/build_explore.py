#!/usr/bin/env python3

import json, os, re, sys
from tools import quran, corpus, usable, hisn, PROJECT
from check import verse_texts

HERE = os.path.dirname(os.path.abspath(__file__))
GROUPS = [("faith", "Faith"), ("worship", "Worship"), ("character", "Character"), ("family", "Family"),
          ("society", "Society"), ("daily-life", "Daily Life"), ("sins", "Sins to Avoid")]
SOURCE = ("Chosen for each topic from the Quran (Uthmani Arabic, Saheeh International, Fateh Muhammad Jalandhry; "
          "part-verses cut to the same words in all three), hadith graded sahih or hasan in HadeethEnc's published "
          "Arabic, English and Urdu (in full, unmodified), and duas from the bundled Hisn al-Muslim, listed by number.")

def hadith_entry(r):
    assert usable(r), r["id"]
    return {"arabic": r["arabic"].strip(),
            "translation": {"en": r["text_en"].strip(), "ur": r["text_ur"].strip()},
            "attribution": {"en": r["attribution_en"].strip(), "ur": r["attribution_ur"].strip()},
            "grade": {"en": r["grade_en"].strip(), "ur": r["grade_ur"].strip()},
            "source": "HadeethEnc.com"}

def verse_entry(v, q):
    texts = verse_texts(v["ref"], q)
    assert texts, v["ref"]
    cut = v.get("cut")
    if cut:
        for l in ("ar", "en", "ur"):
            assert cut[l] in texts[l], (v["ref"], l)
        texts = {l: cut[l] for l in ("ar", "en", "ur")}
        texts["en"] = texts["en"][:1].upper() + texts["en"][1:]
    return {"reference": v["ref"].replace("-", "–"), "arabic": texts["ar"].strip(),
            "translation": {"en": texts["en"].strip(), "ur": texts["ur"].strip()}}

def build_explore():
    q, c = quran(), corpus()
    hisn_ids = {int(d["id"]): d for _, d in hisn()}
    icons = json.load(open(os.path.join(HERE, "icons.json")))
    header = verse_texts("29:69", q)
    out = {"source": SOURCE,
           "header": {"reference": "29:69", "translation": {"en": header["en"], "ur": header["ur"]}},
           "groups": []}
    seen = set()
    for gid, title in GROUPS:
        if not os.path.exists(os.path.join(HERE, "selections", f"{gid}.json")):
            print("not ready yet:", gid); continue
        data = json.load(open(os.path.join(HERE, "selections", f"{gid}.json")))
        topics = []
        for t in data["topics"]:
            assert t["id"] not in seen, t["id"]
            seen.add(t["id"])
            duas = []
            for d in t.get("duas", []):
                e = hisn_ids[int(d["hisnId"])]
                assert e.get("translationUrdu"), d
                duas.append(int(d["hisnId"]))
            topics.append({"id": t["id"], "title": t["title"], "icon": f"{icons[t['id']]}-linear",
                           "keywords": t.get("keywords", []),
                           "verses": [verse_entry(v, q) for v in t["quran"]],
                           "hadith": [hadith_entry(c[str(h["id"])]) for h in t["hadith"]],
                           "duas": duas})
        out["groups"].append({"id": gid, "title": title, "topics": topics})
    path = os.path.join(PROJECT, "Explore.json")
    json.dump(out, open(path, "w"), ensure_ascii=False, indent=1)
    n = sum(len(g["topics"]) for g in out["groups"])
    print(f"Explore.json: {n} topics, {os.path.getsize(path) // 1024} KB")

def add_arabic_to_emotions():
    c = corpus()
    by_en = {r["text_en"].strip(): r for r in c.values()}
    path = os.path.join(PROJECT, "Emotions.json")
    book = json.load(open(path))
    missing = 0
    for e in book["emotions"]:
        for h in e["hadith"]:
            r = by_en.get(h["translation"]["en"].strip())
            if not r:
                missing += 1; print("no match:", e["id"], h["translation"]["en"][:80]); continue
            new = {"arabic": r["arabic"].strip()}
            new.update({k: v for k, v in h.items() if k != "arabic"})
            h.clear(); h.update(new)
    json.dump(book, open(path, "w"), ensure_ascii=False, indent=1)
    print(f"Emotions.json: Arabic added, {missing} unmatched")

if __name__ == "__main__":
    add_arabic_to_emotions()
    if sys.argv[1:] != ["emotions"]:
        build_explore()
