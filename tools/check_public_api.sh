#!/bin/sh
set -eu

repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
update_baseline=false
if [ "${1:-}" = "--update" ]; then
  update_baseline=true
  shift
fi
aar_path=${1:-"$repo_dir/core/build/outputs/aar/core-release.aar"}
baseline="$repo_dir/core/api/current.txt"

if [ ! -f "$aar_path" ]; then
  echo "Missing AAR: $aar_path" >&2
  exit 1
fi

api_tmp=$(mktemp -d "${TMPDIR:-/tmp}/smartisan-public-api.XXXXXX")
cleanup() {
  case "$api_tmp" in
    "${TMPDIR:-/tmp}"/smartisan-public-api.*) rm -r -- "$api_tmp" ;;
    *) echo "Refusing to remove unexpected temp directory: $api_tmp" >&2 ;;
  esac
}
trap cleanup EXIT HUP INT TERM

unzip -p "$aar_path" classes.jar > "$api_tmp/classes.jar"
jar tf "$api_tmp/classes.jar" \
  | sed -n 's#/#.#g; s#\.class$##p' \
  | grep '^org\.opensmartisanos\.ui\.' \
  | grep -v '^org\.opensmartisanos\.ui\.internal\.' \
  | grep -v '\.R\($\|\$\)' \
  | grep -v '\.BuildConfig$' \
  | LC_ALL=C sort > "$api_tmp/candidates.txt"

filter_public_classes() {
  awk '
    /^Compiled from / { block = $0 ORS; public_class = 0; next }
    {
      block = block $0 ORS
      if ($0 ~ /^public /) public_class = 1
      if ($0 == "}") {
        if (public_class) printf "%s", block
        block = ""
        public_class = 0
      }
    }
  '
}

# A single javap VM can inspect the complete AAR in a fraction of the time of
# starting one VM per class. Package-private implementation classes remain in
# the input so nested public classes are found; the filter removes their blocks.
xargs javap -classpath "$api_tmp/classes.jar" -public -constants \
  < "$api_tmp/candidates.txt" | filter_public_classes > "$api_tmp/current.txt"
xargs javap -classpath "$api_tmp/classes.jar" -protected -s \
  < "$api_tmp/candidates.txt" | filter_public_classes > "$api_tmp/current-access.txt"

hash_update_arg=
if [ "$update_baseline" = true ]; then
  hash_update_arg=--update-mapped-hashes
fi
python3 "$repo_dir/tools/check_api_compatibility.py" \
  --v1 "$repo_dir/core/api/v1.txt" \
  --current "$api_tmp/current.txt" \
  --current-access "$api_tmp/current-access.txt" \
  --mapping "$repo_dir/core/api/r2-mapping.json" \
  --r2-access "$repo_dir/core/api/r2-dex-access.txt" \
  $hash_update_arg

if [ "$update_baseline" = true ]; then
  cp "$api_tmp/current.txt" "$baseline"
  echo "Updated complete public API baseline after v1/R2 validation."
  exit 0
fi

if ! diff -u "$baseline" "$api_tmp/current.txt"; then
  echo "Public API compatibility check failed." >&2
  exit 1
fi
echo "Complete public API compatibility check passed."
