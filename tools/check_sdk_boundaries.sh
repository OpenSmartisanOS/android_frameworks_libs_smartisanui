#!/bin/sh
set -eu

repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$repo_dir"

fail_if_match() {
  description=$1
  pattern=$2
  shift 2
  if rg -n --glob '*.{java,kt,xml,gradle,kts,bp}' "$pattern" "$@"; then
    echo "Boundary check failed: $description" >&2
    exit 1
  fi
}

fail_if_match "Core/Catalog must not depend on AndroidX or Google Material" \
  'androidx\.|android\.support\.|com\.google\.android\.material|com\.android\.internal' core catalog
fail_if_match "Removed compatibility shells must not return" \
  '\bSmartisan(AppBarLayout|CollapsingToolbarLayout|FloatingToolbarLayout|ExtendedButton|CardView|EditText|TextInputLayout|Slider|TabLayout|TabLayoutMediator|BottomSheetDialog|BottomSheetDialogFragment)\b' \
  core catalog system
fail_if_match "Settings application colors do not belong in Core" \
  'smartisan_sos_' core catalog

if rg -n '^android\.useAndroidX=true$' gradle.properties; then
  echo "Boundary check failed: AndroidX mode must stay disabled" >&2
  exit 1
fi

if ! rg -q 'sdk_version: "current"' core/Android.bp; then
  echo "Boundary check failed: Soong build must use the public current SDK" >&2
  exit 1
fi

python3 -m json.tool core/provenance/r2-settings-components-resources.json >/dev/null
python3 -m json.tool core/provenance/r2-dialog-resources.json >/dev/null
python3 -m json.tool core/api/r2-mapping.json >/dev/null
python3 tools/check_dialog_contracts.py
python3 tools/rom/extract_resources.py \
  --manifest core/provenance/r2-dialog-resources.json \
  --output core/src/main/res \
  --check
python3 tools/rom/extract_resources.py \
  --manifest core/provenance/r2-settings-components-resources.json \
  --output core/src/main/res \
  --check
python3 tools/check_resource_provenance.py \
  --manifest core/provenance/r2-dialog-resources.json \
  --manifest core/provenance/r2-settings-components-resources.json \
  --resource-root core/src/main/res \
  --baseline-ref b8751dadeffab14d14cbff4a54c06605dcfe4366
common_bg=core/src/main/res/drawable-xxhdpi/smartisan_rom_common_bg.9.png
expected_common_bg=b8a4c7911ccf63a27e5c48b1f860dffc92c29059f22342de13970e06722d6d38
actual_common_bg=$(shasum -a 256 "$common_bg" | awk '{print $1}')
if [ "$actual_common_bg" != "$expected_common_bg" ]; then
  echo "Boundary check failed: common Settings background provenance mismatch" >&2
  exit 1
fi

for canonical in core/src/main/res/drawable-xhdpi/smartisan_rom_btn_check_*_canonical.png; do
  original=${canonical%_canonical.png}.png
  if ! cmp -s "$original" "$canonical"; then
    echo "Boundary check failed: canonical checkbox asset changed: $canonical" >&2
    exit 1
  fi
done
for adapter in \
  SmartisanCheckBox \
  SmartisanRadioButton \
  SmartisanProgressBar \
  SmartisanCircularProgressBar \
  SmartisanSpinner
do
  if ! rg -q "org\.opensmartisanos\.ui\.widget\.$adapter" \
      core/provenance/r2-settings-components-resources.json; then
    echo "Missing provenance classification for $adapter" >&2
    exit 1
  fi
done

echo "SDK dependency and resource ownership checks passed."
