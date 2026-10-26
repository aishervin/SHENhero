# SHENhero

Local-first tools for Android and Termux.

## opencode web UI for Termux

Run the real opencode web UI on Android, served from your own local opencode
server. The UI is built from the original opencode v1.19.4 `packages/app` source,
with editable source vendored in this repository and an optional RTL fix.

The proxy binds to `127.0.0.1`, starts `opencode serve` with a random per-launch
password, and forwards the API, SSE events and WebSockets. No hosted backend or
third-party account is involved.

### Install

```bash
curl -fsSL https://raw.githubusercontent.com/aishervin/SHENhero/main/termux-skin/install.sh | bash
```

Requirements: Termux, `curl`, `python`, and opencode. The installer adds
opencode if it is missing, downloads the proxy and prebuilt UI, then opens the
local URL in your browser.

### Commands

```bash
opencode-web-open          # start the server and UI
opencode-web-open ~/code   # start in a project directory
opencode-web-stop          # stop both processes
opencode-web-update        # update the proxy, launchers and UI
```

### Edit the UI

```bash
git clone https://github.com/aishervin/SHENhero
cd SHENhero/termux-skin
# edit ui-src/packages/...
./ui-src/build.sh
OC_UI_DIR="$PWD/ui" opencode-web-open
```

The build clones `sst/opencode` at the matching tag, overlays the vendored source,
and builds `packages/app` on-device.

Termux uses the prebuilt stylesheet because Tailwind's `lightningcss` dependency
has no Android arm64 npm binding. Add style overrides in `ui-src/custom.css`, or
run a full Tailwind build on a glibc machine with `OPENCODE_UI_TAILWIND=1`.

### Layout

```
termux-skin/
  install.sh          installer
  bin/                launchers and updater
  proxy/              local UI/API proxy with RTL fix
  ui-src/             original UI source and build script
  ui.tar.gz           prebuilt UI served by the proxy
  index.html          project landing page
```

See [termux-skin/README.md](termux-skin/README.md) for configuration, security
notes and build details.

## License

MIT. opencode is a separate MIT-licensed project; this repository is an
independent packaging and is not affiliated with it.
