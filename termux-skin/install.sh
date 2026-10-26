#!/data/data/com.termux/files/usr/bin/env bash
set -euo pipefail

REPO="${OC_SKIN_REPO:-aishervin/SHENhero}"
BASE="${OC_SKIN_BASE:-termux-skin}"
RAW="https://raw.githubusercontent.com/$REPO/main/$BASE"

red() { printf '\033[0;31m%s\033[0m\n' "$*" >&2; }
grn() { printf '\033[0;32m%s\033[0m\n' "$*"; }
say() { printf '  %s\n' "$*"; }

if [ -z "${PREFIX:-}" ] || [ ! -d "$PREFIX" ]; then
  red "This installer targets Termux. \$PREFIX is not set."
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  say "Installing curl"
  pkg install -y curl >/dev/null
fi

if ! command -v opencode >/dev/null 2>&1; then
  grn "opencode is not installed; installing it first"
  curl -fsSL https://raw.githubusercontent.com/bd-loser/opencode-bionic/main/install.sh | bash
fi

grn "Installing the opencode skin launcher"
mkdir -p "$PREFIX/bin" "${HOME:-$PREFIX}/.opencode-web"
curl -fsSL "$RAW/bin/opencode-web-update" -o "$PREFIX/bin/opencode-web-update"
chmod +x "$PREFIX/bin/opencode-web-update"
"$PREFIX/bin/opencode-web-update"

grn "Ready. Full functionality runs on your local opencode server."
exec "$PREFIX/bin/opencode-web-open" "$@"
