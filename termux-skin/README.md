# SHΞN ʜᴇʀᴏ™ Termux Agent Coder

A local-first coding workspace for Android and Termux, with the familiar OpenCode UI and workflows branded for SHΞN ʜᴇʀᴏ™.

The local proxy serves the bundled UI and forwards API requests, SSE events, and WebSockets to the coding server on loopback. It also includes an optional RTL text fix. No hosted SHΞN ʜᴇʀᴏ™ backend or third-party account is required.

## Install

```bash
curl -fsSL https://raw.githubusercontent.com/aishervin/SHENhero/main/termux-skin/install.sh | bash
```

The installer:
- installs the compatible coding engine if it is missing;
- downloads the local proxy, launchers, and prebuilt UI;
- starts the local server with a random password;
- serves the UI on loopback and opens the workspace in a browser.

Requirements: Termux, `curl`, `python`, and the compatible coding engine. `bun` and `git` are only needed when rebuilding the UI.

## Commands

```bash
opencode-web-open          # start the local workspace
opencode-web-open ~/code   # start in a project directory
opencode-web-stop          # stop local processes
opencode-web-update        # update launcher and prebuilt UI
```

The local server state is stored under `~/.opencode-web/`.

## Edit and rebuild the UI

Editable UI sources are in `ui-src/`:

```bash
git clone https://github.com/aishervin/SHENhero
cd SHENhero/termux-skin
# edit files under ui-src/packages/
./ui-src/build.sh
OC_UI_DIR="$PWD/ui" opencode-web-open
```

The build overlays the editable UI sources on the matching upstream source version, builds the app, and refreshes `ui.tar.gz`. To pin a version, set `OPENCODE_UI_VERSION`. Tailwind's native CSS dependency is not available for Android arm64, so the default Termux build uses the included compiled stylesheet and appends `ui-src/custom.css` when present.

## Local architecture

```
browser -> 127.0.0.1:<proxy> -> bundled UI
                            -> local coding server
```

The proxy listens on `127.0.0.1` only, generates a random password for the local server, and forwards API, SSE, and WebSocket traffic. Do not expose the proxy through a reverse tunnel without adding authentication.

## Source and license notices

The UI includes code derived from the upstream OpenCode project. Its original copyright notices and MIT license terms must be preserved for applicable files. SHΞN ʜᴇʀᴏ™ is an independent customization for Termux and is not an official upstream release.

Project: https://github.com/aishervin/SHENhero
Issues: https://github.com/aishervin/SHENhero/issues
