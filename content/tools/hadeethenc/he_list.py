import json, urllib.request, time
def get(url):
    for i in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent':'curl/8.4.0'}), timeout=30) as r:
                return json.loads(r.read())
        except Exception as e:
            time.sleep(2)
    raise Exception(url)
ids = {}
for cat in range(1,8):
    page=1
    while True:
        d = get(f"https://hadeethenc.com/api/v1/hadeeths/list/?language=en&category_id={cat}&page={page}&per_page=100")
        for h in d['data']:
            ids[h['id']] = {'title':h['title'], 'ur': 'ur' in h['translations']}
        meta = d.get('meta',{})
        if page >= int(meta.get('last_page',1)): break
        page+=1
    print(cat, len(ids), meta)
json.dump(ids, open('he/list_en.json','w'), ensure_ascii=False)
