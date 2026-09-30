import json,sys
from tools import quran, corpus
q,c=quran(),corpus()
d=json.load(open(sys.argv[1]))
for t in d["topics"]:
    print(f"## {t['title']}  [{', '.join(t['keywords'])}]")
    for v in t["quran"]:
        r=v["ref"].split("-")[0]
        txt=(v["cut"] or {}).get("en") or q["en"][r]
        print(f"  Q {v['ref']}{' (cut)' if v['cut'] else ''}: {txt[:110]}")
    for h in t["hadith"]:
        r=c[str(h["id"])]
        print(f"  H {h['id']} ({r['grade_en']}, {len(r['text_en'])}ch): {r['text_en'][:110]}")
    for x in t["duas"]:
        print(f"  D {x['hisnId']}: {x.get('why','')[:90]}")
