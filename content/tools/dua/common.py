import re,json,html,unicodedata
HISN="/Users/vyro/Projects/IOS Projects/Muttaqi/Muttaqi/Resources/Data/HisnAlMuslim.json"
def load_hisn():
    d=json.load(open(HISN))
    out=[]
    for c in d['categories']:
        for ch in c['chapters']:
            for du in ch['duas']:
                x=dict(du); x['chapter']=ch['id']; x['chapterTitle']=ch['titleArabic']; x['chapterEn']=ch['title']
                out.append(x)
    return sorted(out,key=lambda x:x['id'])
DIAC=re.compile(r'[ؐ-ًؚ-ٰٟۖ-ۭ࣓-ࣿـ]')
def norm_ar(s):
    s=unicodedata.normalize('NFKC',s)
    s=DIAC.sub('',s)
    s=re.sub('[إأآٱا]','ا',s)
    s=s.replace('ى','ي').replace('ی','ي').replace('ة','ه').replace('ۃ','ه').replace('ہ','ه').replace('ؤ','و').replace('ئ','ي').replace('ک','ك').replace('ء','')
    s=re.sub(r'[^ء-ي\s]',' ',s)
    s=re.sub(r'\s+',' ',s).strip()
    return s
def toks(s): return norm_ar(s).split()
def plain(h):
    h=re.sub(r'<br\s*/?>',' ⏎ ',h)
    return re.sub(r'\s+',' ',html.unescape(re.sub(r'<[^>]+>','',h))).strip()

def iub_plain(h):
    h=re.sub(r'<br\s*/?>','\n',h)
    t=html.unescape(re.sub(r'<[^>]+>','',h))
    t=re.sub(r'[ \t ]+',' ',t)
    t=re.sub(r' *\n *','\n',t)
    return t.strip()
def iub_extract(h):
    """Return the Urdu translation portion: text before first reference/note, trimmed to the quoted part."""
    cut=len(h)
    for pat in ['<span class = "reference','<span class="reference','نوٹ:-','نوٹ :-']:
        k=h.find(pat)
        if k!=-1: cut=min(cut,k)
    t=iub_plain(h[:cut]).strip()
    t=re.split(r'\n\s*نوٹ',t)[0].strip()
    return t

def quote_span(t):
    """Return (text, npairs, outside) : span from first ” to last “."""
    a=t.find('”'); b=t.rfind('“')
    if a==-1 or b==-1 or b<a: return t.strip(),0,''
    n=t.count('”')
    span=t[a:b+1]
    outside=(t[:a]+' … '+t[b+1:]).strip(' …\n')
    if n==1 and span.count('“')==1:
        span=span[1:-1].strip()
    return span.strip(),n,outside

def first_seg(t):
    a=t.find('”')
    if a==-1: return t.strip(), '', 0
    b=t.find('“',a)
    if b==-1: return t.strip(), '', 0
    n=t.count('”')
    rest=(t[:a]+' … '+t[b+1:]).strip(' …\n')
    return t[a+1:b].strip(), rest, n
