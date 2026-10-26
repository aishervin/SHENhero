# opencode web UI for Termux

Run the real opencode web UI locally on Android/Termux, then edit its source and
rebuild it on-device.

This project packages the original `packages/app` UI from opencode v1.19.4. A
small local proxy serves the UI and forwards `/session`, `/config`, `/event`,
`/pty` and the rest of the API to your own `opencode serve` process. Nothing is
sent to a third-party server; the server and proxy bind to `127.0.0.1`.

The proxy also injects an optional RTL/Arabic text fix.

## Install

```bash
curl -fsSL https://raw.githubusercontent.com/aishervin/SHENhero/main/termux-skin/install.sh | bash
```

The installer:

- installs `opencode` if it is missing;
- downloads the proxy, launchers and the prebuilt `ui.tar.gz`;
- starts `opencode serve --port 0` with a random local password;
- starts the proxy and opens `http://127.0.0.1:<port>/` in your browser.

Requirements: Termux, `curl`, `python`, and opencode. `bun` and `git` are only
needed when rebuilding the UI.

## Commands

```bash
opencode-web-open          # start the server + UI and open the browser
opencode-web-open ~/code   # start in a project directory
opencode-web-stop          # stop both processes
opencode-web-update        # download the latest proxy, launchers and UI
```

State is kept in `~/.opencode-web/` (`state`, `server.log`, `proxy.log`, `ui/`).

## Edit the original UI

The editable source is in `ui-src/`. It was extracted from the v1.19.4 source map
and keeps the original path layout:

```
ui-src/packages/app/src/...
ui-src/packages/ui/src/...
ui-src/packages/session-ui/src/...
ui-src/packages/core/src/...
ui-src/packages/sdk/js/src/...
ui-src/packages/schema/src/...
```

Build a custom UI:

```bash
git clone https://github.com/aishervin/SHENhero
cd SHENhero/termux-skin
# edit files under ui-src/packages/
./ui-src/build.sh
OC_UI_DIR="$PWD/ui" opencode-web-open
```

`build.sh` clones `sst/opencode` at the matching tag, overlays `ui-src/packages`,
installs dependencies, builds `packages/app`, writes `./ui/` and rebuilds
`ui.tar.gz`.

Pin a version with `OPENCODE_UI_VERSION=1.19.4`. Use a different work directory
with `OPENCODE_UI_WORK=/path`. Use `OPENCODE_UI_OUT=/path` for another output.

### Styles

Tailwind v4 uses `lightningcss`, which does not publish an Android arm64 npm
binding. On Termux the default build therefore uses `ui-src/prebuilt.css` (the
exact compiled stylesheet from v1.19.4) and skips the Tailwind plugin.

- Put custom CSS in `ui-src/custom.css`; it is appended to the stylesheet.
- To build CSS from Tailwind sources on a glibc machine, run with
  `OPENCODE_UI_TAILWIND=1`.
- Without that, new Tailwind utility classes added to TSX will not be generated.

## How it works

```
browser -> 127.0.0.1:<proxy> -> local UI files
                            -> opencode serve 127.0.0.1:<random>
```

- `opencode serve --port 0` picks a free port and prints it.
- The proxy adds HTTP Basic auth for the upstream server using the generated
  `OPENCODE_SERVER_PASSWORD`, forwards SSE and WebSockets, and injects the RTL
  script into HTML responses.
- If `OC_UI_DIR/index.html` exists, the proxy serves that UI and uses it as the
  SPA fallback. Otherwise it uses the UI shipped with the installed opencode.

## Environment

| Variable | Meaning |
| --- | --- |
| `OC_UI_DIR` | UI directory to serve (default `~/.opencode-web/ui`) |
| `OC_UI_SKIP=1` | Keep the current UI during `opencode-web-update` |
| `OPENCODE_WEB_PORT` | Fixed proxy port (default: random) |
| `OPENCODE_WEB_NO_OPEN=1` | Do not launch a browser |
| `OPENCODE_SERVER_PASSWORD` | Reuse a specific server password |
| `OC_SKIN_REPO` | GitHub repo for updates (default `aishervin/SHENhero`) |
| `OC_SKIN_BASE` | Subdirectory in that repo (default `termux-skin`) |

## Security

- The proxy listens on `127.0.0.1` only.
- The opencode server uses a random password per launch, passed to the proxy
  through the environment.
- Do not expose the proxy port through a reverse tunnel without adding your own
  authentication.

## Source and license

The UI source comes from the opencode v1.19.4 build (`packages/app` and its
workspace packages). opencode is MIT licensed. This repository is an independent
Termux packaging and is not affiliated with the opencode project.
