import json,glob,sys,re
sys.path.insert(0,'.')
from build import clean_norm
idx=json.load(open('he/index.json'))
DB={}
for f in glob.glob('he/ar/*.json'):
    try: d=json.load(open(f))
    except: continue
    DB[d['id']]=(clean_norm(d.get('hadeeth','')), d)
def find(*terms, show=250):
    res=[]
    ts=[clean_norm(t) for t in terms]
    for i,(t,d) in DB.items():
        if all(x in t for x in ts): res.append(i)
    return res
if __name__=='__main__':
    for q in sys.argv[1:]:
        terms=q.split('&')
        r=find(*terms)
        print('##',q,'->',len(r))
        for i in r[:12]:
            d=DB[i][1]
            print('  ',i,'en' if i in idx['en'] else '--','ur' if i in idx['ur'] else '--','|',d.get('attribution'),'|',d.get('grade'),'|',d['hadeeth'][:160].replace('\n',' '))
