import urllib.request,os,time
from concurrent.futures import ThreadPoolExecutor
def get(n):
    p=f'iub/h{n}.html'
    if os.path.exists(p) and os.path.getsize(p)>5000: return 'skip'
    u=f"https://islamicurdubooks.com/hadith/hadith-.php?bookid=19&hadith_number={n}"
    for a in range(4):
        try:
            req=urllib.request.Request(u,headers={'User-Agent':'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'})
            b=urllib.request.urlopen(req,timeout=60).read()
            open(p,'wb').write(b); return 'ok'
        except Exception as e:
            time.sleep(3*(a+1))
    return 'fail'
with ThreadPoolExecutor(4) as ex:
    res=list(ex.map(get,range(1,281)))
from collections import Counter
print(Counter(res))
