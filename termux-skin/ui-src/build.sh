#!/data/data/com.termux/files/usr/bin/env bash
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/.." && pwd)"
UPSTREAM="${OPENCODE_UI_UPSTREAM:-https://github.com/sst/opencode}"
VERSION="${OPENCODE_UI_VERSION:-}"
if [ -z "$VERSION" ]; then
  VERSION="$(opencode --version 2>/dev/null | head -n1 || true)"
fi
VERSION="${VERSION#v}"
TAG="v${VERSION:-1.19.4}"
WORK="${OPENCODE_UI_WORK:-${PREFIX:-/tmp}/tmp/opencode-ui-build}"
SRC="$WORK/opencode"
OUT="${OPENCODE_UI_OUT:-$REPO/ui}"

say() { printf '  %s\n' "$*"; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

command -v bun >/dev/null 2>&1 || die "bun is required (pkg install bun)"
command -v git >/dev/null 2>&1 || die "git is required (pkg install git)"

if [ ! -d "$SRC/.git" ]; then
  say "Cloning $UPSTREAM at $TAG"
  rm -rf "$WORK"
  mkdir -p "$WORK"
  git clone --depth 1 --branch "$TAG" "$UPSTREAM" "$SRC"
else
  say "Reusing clone at $SRC"
fi

say "Overlaying editable sources from $HERE/packages"
(cd "$HERE" && tar cf - packages) | (cd "$SRC" && tar xf -)

if [ "${OPENCODE_UI_TAILWIND:-0}" = "1" ]; then
  say "Keeping Tailwind build (requires a lightningcss native binding)"
else
  say "Using prebuilt stylesheet (lightningcss has no Android arm64 binding)"
  [ -f "$HERE/prebuilt.css" ] || die "missing $HERE/prebuilt.css"
  sed -i '/@tailwindcss\/vite/d; /^[[:space:]]*tailwindcss(),[[:space:]]*$/d' "$SRC/packages/app/vite.js"
  cp "$HERE/prebuilt.css" "$SRC/packages/app/src/index.css"
  if [ -f "$HERE/custom.css" ]; then
    printf '\n/* custom.css */\n' >> "$SRC/packages/app/src/index.css"
    cat "$HERE/custom.css" >> "$SRC/packages/app/src/index.css"
  fi
fi

say "Installing dependencies (this can take a while)"
if [ -f "$SRC/bun.lock" ]; then
  (cd "$SRC" && bun install --frozen-lockfile --ignore-scripts)
else
  (cd "$SRC" && bun install --ignore-scripts)
fi

export NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=2048}"
sed -i "s/sourcemap: true/sourcemap: false/" "$SRC/packages/app/vite.config.ts"
say "Building packages/app"
(cd "$SRC/packages/app" && bun run build)

[ -f "$SRC/packages/app/dist/index.html" ] || {
  shopt -s nullglob
  candidates=("$SRC"/packages/app/dist/*/index.html)
  [ "${#candidates[@]}" -gt 0 ] || die "build produced no dist/index.html"
  DIST="$(dirname "${candidates[0]}")"
}
DIST="${DIST:-$SRC/packages/app/dist}"

say "Copying built UI to $OUT"
rm -rf "$OUT"
mkdir -p "$OUT"
cp -a "$DIST/." "$OUT/"
rm -f "$OUT"/*.map "$OUT"/assets/*.map
if [ -d "$HERE/prebuilt-assets" ]; then
  say "Adding static assets used by the prebuilt stylesheet and audio cues"
  mkdir -p "$OUT/assets"
  (cd "$HERE/prebuilt-assets" && tar cf - .) | (cd "$OUT/assets" && tar xf -)
fi

say "Packaging $REPO/ui.tar.gz"
tar czf "$REPO/ui.tar.gz" -C "$OUT" .

say "Done."
say "Local UI: $OUT"
say "Tarball:  $REPO/ui.tar.gz"
say "Restart:  opencode-web-stop && opencode-web-open"
