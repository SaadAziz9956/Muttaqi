#!/bin/sh
# Converts the Iconsax SVGs in the iOS asset catalogue into Android vector drawables (res/drawable/ic_<name>.xml),
# so both apps use exactly the same icons. Run again after adding an icon to the iOS catalogue.
set -e
cd "$(dirname "$0")/.."
OUT=androidApp/src/main/res/drawable
TMP=$(mktemp -d)
for svg in iosApp/Muttaqi/Resources/Assets.xcassets/Icons/*.imageset/*.svg; do
    name=$(basename "$svg" .svg | tr '[:upper:]-' '[:lower:]_')
    cp "$svg" "$TMP/ic_$name.svg"
done
npx --yes svg2vectordrawable@2 -f "$TMP" -o "$OUT" >/dev/null
rm -rf "$TMP"
echo "$(ls $OUT/ic_*.xml | wc -l | tr -d ' ') icons in $OUT"
