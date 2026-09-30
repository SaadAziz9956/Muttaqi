# Content pipeline

Every religious text in Muttaqi is copied word for word from a published source. The app never generates,
paraphrases or machine-translates. These scripts are how the bundled JSON is built. Researchers (people or agents)
only **choose references**; the scripts copy the text from the sources and reject anything that isn't allowed.

## Sources

| Text | Arabic | English | Urdu |
|---|---|---|---|
| Quran | `quran-uthmani` | Saheeh International (`en.sahih`) | Fateh Muhammad Jalandhry (`ur.jalandhry`) |
| Hadith | HadeethEnc | HadeethEnc | HadeethEnc |
| Duas | Hisn al-Muslim | Hisn al-Muslim | Hisn al-Muslim (IslamicUrduBooks edition) |
| 99 Names | Tirmidhi 3507 | Darussalam | al-Faryiwa'i |

Quran editions come from api.alquran.cloud; hadith from the HadeethEnc API (only those graded sahih or hasan with
both English and Urdu). Licensing permissions for these sources are still needed before release.

## Rebuilding

Downloaded sources go in `cache/`, which isn't committed:

- `cache/editions/{quran-uthmani,en.sahih,ur.jalandhry}.json`: `https://api.alquran.cloud/v1/quran/<edition>`
- `cache/he/{ar,en,ur}/<id>.json` and `cache/he_corpus.json`: `hadeethenc/he_list.py` then `he_fetch.py` for each
  language, then the corpus step at the top of the Explore session (`explore/tools.py` reads it)

Then, for Explore and the hadith Arabic in Emotions:

```
cd content/tools/explore
python3 check.py selections/worship.json   # each group: every reference exists, cuts are exact, grades allowed
python3 build_explore.py                   # writes content/data/Explore.json (and Emotions' Arabic)
```

A rebuild from the committed selections reproduces the shipped `Explore.json` byte for byte.

## Folders

- `explore/`: tools, checker, builder, the research brief, icons and the seven curated `selections/`, plus
  `decisions.md` for every hand edit made after research
- `emotions/`: the curated research behind `Emotions.json`
- `dhikr/`, `names/`, `dua/`, `hadeethenc/`: the scripts used for Dhikr, the 99 Names, Hisn al-Muslim's Urdu and
  the HadeethEnc downloads. They were first run from a scratch folder, so some still have absolute paths to update
  before running again.
