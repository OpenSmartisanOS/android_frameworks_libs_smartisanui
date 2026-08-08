#!/bin/sh
set -eu

repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
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

: > "$api_tmp/current.txt"
while IFS= read -r class_name; do
  javap -classpath "$api_tmp/classes.jar" -public -constants "$class_name" \
    > "$api_tmp/class.txt"
  if sed -n '2p' "$api_tmp/class.txt" | grep -q '^public '; then
    cat "$api_tmp/class.txt" >> "$api_tmp/current.txt"
  fi
done < "$api_tmp/candidates.txt"

if ! diff -u "$baseline" "$api_tmp/current.txt"; then
  echo "Public API compatibility check failed." >&2
  exit 1
fi
echo "Complete public API compatibility check passed."
