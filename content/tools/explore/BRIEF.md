# Explore content brief

An iOS Islamic app ("Muttaqi") has an Explore tab: topics such as "Charity & Zakat" or "Drugs & Alcohol". Each topic
page shows Quran verses, authentic hadith and duas about it, in English and Urdu.

## Hard rules (the app owner's)
- Every text is verbatim from a published translation. You never write, paraphrase or translate anything: you only
  CHOOSE references. A build script copies the text from the published sources below.
- Hadith must be graded sahih or hasan, from HadeethEnc (scholar-reviewed, English and Urdu). Only use a hadith the tools
  show as `usable: yes`.
- Content must speak directly to the topic. No loosely related filler. Fewer strong items beat many weak ones.
- Prefer texts that are clear on their own, without needing tafsir or legal context to be understood correctly. If you
  leave out a well-known text for that reason, say so in your report.

## Tools (local, fast; run from the explore folder)
    cd /private/tmp/claude-501/-Users-vyro-Projects-IOS-Projects-Muttaqi/58111274-270b-439e-81ca-c9cc28a6407b/scratchpad/explore
    python3 tools.py            # prints usage
- `ayah 2:274`: Arabic (Uthmani), English (Saheeh International) and Urdu (Fateh Muhammad Jalandhry)
- `qsearch word word` / `qsearch-ur`: find verses by words in the English / Urdu
- `hsearch word word` / `hsearch-ar`: find HadeethEnc hadith by English / Arabic words
- `cats`, then `cat ID`: browse HadeethEnc's own topic categories (very useful, e.g. Zakat, Fasting, Marriage)
- `hadith ID`: full text, grade and attribution
- `hisn word`, then `hisnid N`: duas in the app's bundled Hisn al-Muslim (English and Urdu)
You know the Quran and hadith well. Use that knowledge to decide what to look up, then confirm every item with the tools.

## Per topic
- **Quran: 4 to 6 entries.** Each is one verse ("2:274") or a short run of up to 3 consecutive verses ("59:22-24").
  Prefer whole verses. If only part of a verse fits, give a `cut`: the exact words to keep from each of the three texts.
  Each must be an exact substring of what `ayah` prints, and the three must cover the same meaning.
- **Hadith: 2 to 4 entries** (5 at most), each with a HadeethEnc id. The app shows HadeethEnc's full text unmodified.
  So when two hadith fit equally well, prefer the shorter one (under about 700 English characters).
- **Duas: 0 to 3** Hisn al-Muslim ids, only where a dua genuinely belongs to the topic (e.g. Fasting: breaking the
  fast; Travel: the travel dua; Sickness: visiting the sick). Most topics will have none, which is fine.
- `keywords`: 3 to 8 search words a user might type, in English and romanised Arabic/Urdu (e.g. "zakah", "sadaqah",
  "roza"). Don't repeat the title.
- `why`: one short line per item, for the developer (not shown in the app).

## Output
Write the JSON file named in your task, exactly this shape:
    {"group": "faith", "topics": [
      {"id": "charity-zakat", "title": "Charity & Zakat", "keywords": ["zakah", "sadaqah", "alms"],
       "quran": [{"ref": "2:274", "cut": null, "why": "..."},
                 {"ref": "9:60", "cut": {"ar": "...", "en": "...", "ur": "..."}, "why": "..."}],
       "hadith": [{"id": "5913", "why": "..."}],
       "duas": [{"hisnId": 176, "why": "..."}]}]}
Keep the topic ids and titles exactly as given, in the given order.

Then check your own file:
    python3 check.py YOURFILE.json
Fix everything it reports until it prints OK.

## Report (under 300 words)
Give counts per topic, any topic with thin content and why, well-known texts you left out (weak, or unclear without
context), and anything you were unsure about.
