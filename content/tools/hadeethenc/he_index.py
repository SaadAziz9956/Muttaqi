import json, subprocess, time
def get(url):
    for i in range(4):
        r = subprocess.run(['curl','-s','-m','60',url],capture_output=True,text=True).stdout
        try: return json.loads(r)
        except Exception: time.sleep(2)
    raise RuntimeError(url)
out={}
for lang in ['ar','en','ur']:
    ids={}
    for root in range(1,8):
        page=1
        while True:
            d=get(f'https://hadeethenc.com/api/v1/hadeeths/list/?language={lang}&category_id={root}&page={page}&per_page=500')
            for h in d.get('data',[]):
                ids[h['id']]=h['title']
            last=int(d['meta']['last_page'])
            if page>=last: break
            page+=1
    out[lang]=ids
    print(lang,len(ids))
json.dump(out,open('he/index.json','w'),ensure_ascii=False)
