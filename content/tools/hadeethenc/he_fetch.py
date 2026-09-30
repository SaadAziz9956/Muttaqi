import json, urllib.request, time, os, sys
from concurrent.futures import ThreadPoolExecutor
lang = sys.argv[1]
ids = json.load(open('he/list_en.json'))
todo = [i for i,v in ids.items() if v['ur']] if len(sys.argv)<3 else sys.argv[2].split(',')
os.makedirs(f'he/{lang}', exist_ok=True)
def fetch(i):
    p=f'he/{lang}/{i}.json'
    if os.path.exists(p): return
    url=f"https://hadeethenc.com/api/v1/hadeeths/one/?language={lang}&id={i}"
    for k in range(5):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent':'curl/8.4.0'}), timeout=30) as r:
                data=r.read()
            json.loads(data)
            open(p,'wb').write(data); return
        except Exception as e:
            time.sleep(2+k*2)
    print('FAIL', i)
print(len(todo))
with ThreadPoolExecutor(8) as ex: list(ex.map(fetch, todo))
print('done', len(os.listdir(f'he/{lang}')))
