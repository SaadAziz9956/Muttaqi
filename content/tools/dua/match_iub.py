from common import *
from difflib import SequenceMatcher
H=load_hisn()
I=json.load(open('iub/parsed.json'))
for o in I: o['t']=toks(o['arabic'])
res={}
for x in H:
    ht=toks(x['arabic'])
    best=[]
    for j,o in enumerate(I):
        if not o['t']: continue
        sm=SequenceMatcher(None,ht,o['t'],autojunk=False)
        m=sum(b.size for b in sm.get_matching_blocks())
        cov_h=m/len(ht); cov_i=m/len(o['t'])
        best.append((cov_h*cov_i,cov_h,cov_i,j))
    best.sort(reverse=True)
    b=best[0]; b2=best[1]
    res[x['id']]={'j':b[3],'num':I[b[3]]['num'],'score':round(b[0],3),'cov_h':round(b[1],3),'cov_i':round(b[2],3),'second':(I[b2[3]]['num'],round(b2[0],3)),'hlen':len(ht),'ilen':len(I[b[3]]['t'])}
json.dump(res,open('iub/match.json','w'),indent=1)
import collections
bins=collections.Counter()
for k,v in res.items():
    s=v['score']; bins['>=.9' if s>=.9 else '>=.8' if s>=.8 else '>=.6' if s>=.6 else '>=.4' if s>=.4 else '<.4']+=1
print(bins)
# duplicates: same iub entry mapped to multiple hisn
c=collections.Counter(v['num'] for v in res.values() if v['score']>=0.4)
print('multi-mapped', {k:n for k,n in c.items() if n>1})
for k,v in res.items():
    if v['score']<0.8: print(k,v)
