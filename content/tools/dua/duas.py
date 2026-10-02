import json, unicodedata, re
E = {e: json.load(open(f"editions/{e}.json"))["data"]["surahs"] for e in
     ["quran-uthmani", "en.transliteration", "en.sahih", "ur.jalandhry", "hi.hindi"]}
def text(ed, s, a): return E[ed][s-1]["ayahs"][a-1]["text"]
def bare(w): return "".join(c for c in unicodedata.normalize("NFD", w) if unicodedata.category(c) != "Mn" and c != "ـ")

DUAS = [(2,127,1),(2,128,1),(2,201,1),(2,250,1),(2,286,1),(3,8,0),(3,16,1),(3,38,2),(3,53,0),(3,147,1),
        (3,191,1),(3,192,0),(3,193,0),(3,194,0),(5,83,1),(7,23,1),(7,47,1),(7,126,1),(10,85,1),(14,40,0),
        (14,41,0),(17,24,1),(18,10,1),(20,114,1),(21,89,2),(23,109,1),(23,118,1),(25,65,1),(25,74,1),
        (26,83,0),(27,19,1),(28,24,1),(46,15,1),(59,10,1),(60,4,1),(60,5,0),(66,8,1),(71,28,0)]

def ar_cut(t, occ):
    words = t.split(" ")
    idx = [i for i, w in enumerate(words) if bare(w).startswith("رب") or bare(w).startswith("ورب")]
    return " ".join(words[idx[occ-1]:]) if occ else t, len(idx)
def tr_cut(t, occ):
    words = t.split(" ")
    idx = [i for i, w in enumerate(words) if re.sub(r"[^a-z]", "", w.lower()).startswith("rabb")]
    return " ".join(words[idx[occ-1]:]) if occ else t, len(idx)
def marker_cut(t, occ, pattern):
    if not occ: return t, None
    hits = [m.start() for m in re.finditer(pattern, t)]
    return (t[hits[occ-1]:] if len(hits) >= occ else None), len(hits)

out = []
for s, a, occ in DUAS:
    ar, n_ar = ar_cut(text("quran-uthmani", s, a), occ)
    tl, n_tl = tr_cut(text("en.transliteration", s, a), occ)
    en, n_en = marker_cut(text("en.sahih", s, a), occ, r"(Our|My) Lord")
    ur, n_ur = marker_cut(text("ur.jalandhry", s, a), occ, r"(اے )?(ہمارے |میرے )?پروردگار")
    hi, n_hi = marker_cut(text("hi.hindi", s, a), occ, r"(ऐ )?(हमारे |मेरे )?(रब|पालनहार)")
    out.append(dict(ref=f"{s}:{a}", occ=occ, ar=ar, tl=tl, en=en, ur=ur, hi=hi, counts=(n_ar, n_tl, n_en, n_ur, n_hi)))
json.dump(out, open("duas_draft.json", "w"), ensure_ascii=False, indent=1)
for d in out:
    print(f"--- {d['ref']} occ={d['occ']} counts(ar,tl,en,ur,hi)={d['counts']}")
    for k in ("ar", "tl", "en", "ur", "hi"):
        v = d[k]
        print(f"  {k}: {v[:150] if v else '!!! MISSING'}")
