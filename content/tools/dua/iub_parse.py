import re,html,json,glob
out=[]; seen=set()
for n in range(1,281):
    try: s=open(f'iub/h{n}.html',encoding='utf-8',errors='replace').read()
    except: continue
    # split on hadith number markers
    parts=re.split(r'<span class="fontarabic fs2 hno_color bold">حدیث نمبر: </span><span class="fontcalibri fs1 hno_color bold">',s)
    for p in parts[1:]:
        num=p.split('<',1)[0].strip()
        m=re.search(r'id="tashkeel_[^"]+_with"[^>]*>(.*?)</div>',p,re.S)
        ar=html.unescape(re.sub(r'<[^>]+>','',m.group(1))).strip() if m else ''
        tabs=re.findall(r"data-bs-toggle='tab'[^>]*>([^<]+)</a>",p)
        m2=re.search(r"<div class='fonturdu fs2 hadith_urdu right'>(.*?)</div>\s*</div>",p,re.S)
        urhtml=m2.group(1) if m2 else ''
        key=(num,ar[:80])
        if key in seen: continue
        seen.add(key)
        out.append({'page':n,'num':num,'arabic':ar,'translator':tabs,'urdu_html':urhtml.strip()})
json.dump(out,open('iub/parsed.json','w'),ensure_ascii=False,indent=1)
print(len(out))
from collections import Counter
print(Counter(tuple(o['translator']) for o in out))
print(Counter(o['page']==int(o['num']) if o['num'].isdigit() else 'Q' for o in out))
nums=[o['num'] for o in out]
print(nums[:60])
