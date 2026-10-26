# SHENhero Termux Skin

A graphical skin for the **real** opencode web UI on Termux, plus a small local
proxy that serves it. Every user keeps their own opencode installation, their
own models, providers, auth and sessions. This repo only changes the look.

- Real opencode web UI (chats, tools, files, terminal, permissions — everything)
- Local-only proxy on `127.0.0.1` with a random password
- Injected `skin.css` / `skin.js`, live-editable
- Farsi/Persian RTL fix built in: auto direction, correct joining, Arabic font
- One command install, one command to open, one command to update

This folder lives at `termux-skin/` inside the SHENhero repo, next to the
Android app. Only this folder is needed for the Termux UI.

## Install

```bash
curl -fsSL https://raw.githubusercontent.com/aishervin/SHENhero/main/termux-skin/install.sh | bash
```

If opencode is already installed it is left alone. If it is missing, the
[opencode-bionic](https://github.com/bd-loser/opencode-bionic) installer runs
first.

Requirements: Termux on aarch64, and a browser (Chrome or any Android browser).

## Use

```bash
opencode-web-open          # start server + proxy, open the UI
opencode-web-open ~/code   # start in a project directory
opencode-web-stop          # stop both processes
opencode-web-update        # download the latest skin and proxy
```

The first launch takes a few seconds while the opencode server boots.

Your normal opencode settings are used:

```
~/.config/opencode/          # config, agents, plugins
~/.local/share/opencode/     # auth, sessions, storage
```

Nothing in this repo stores API keys, and the proxy only listens on localhost.

## Customize

Open `index.html` (locally or via GitHub Pages). It is a standalone skin studio:
change colors, radius, font, bubble style, glass and wallpaper, then press
**Generate skin.css**. Copy the result into:

```
~/.opencode-web/skin/skin.css
```

Reload the browser. To publish for everyone, replace `skin/skin.css` in this
repo and let users run `opencode-web-update`.

`skin/skin.js` is optional extra JavaScript. `skin.css` is loaded after the RTL
base styles, so you can override them.

Useful opencode variables to target:

```
--v2-background-bg-deep        app background
--v2-background-bg-base        panels
--v2-background-bg-layer-01/02 message and card surfaces
--v2-background-bg-accent      accent
--v2-text-text-base            main text
--v2-text-text-muted           secondary text
--v2-text-text-accent          links / accent text
--v2-border-border-base        borders
--v2-border-border-focus       focus ring
--v2-font-family-sans          UI font
--radius-sm/md/lg/xl           corner radius
```

Container attributes that are safe to style:

```
[data-component="prompt-input-v2"]
[data-component="dock-prompt"]
[data-slot="user-message-text"]
[data-component="markdown"]
[data-component="markdown-code"]
```

## How it works

```
browser  ->  127.0.0.1:<proxy port>  ->  127.0.0.1:<opencode port>
                    |                          |
             injects skin.css/js         opencode serve
             adds Basic auth             your config + auth
```

`opencode serve` still serves the original UI and API. The Python proxy
(`proxy/rtl-proxy.py`) forwards HTTP, SSE and WebSocket traffic, injects the
skin into the HTML, and adds the server password. Because the UI stays
same-origin, everything works normally.

This is why a static HTML page cannot replace the whole UI: the real app needs
the local API, SSE and WebSocket. The HTML file here is the design studio and
landing page; the full UI remains opencode's own.

## Use your own fork

1. Fork or create a repo with this `termux-skin/` folder.
2. Edit `termux-skin/skin/skin.css` and `termux-skin/index.html`.
3. Users run:

```bash
curl -fsSL https://raw.githubusercontent.com/<you>/<repo>/main/termux-skin/install.sh | OC_SKIN_REPO=<you>/<repo> bash
```

or edit the default in `install.sh`, `bin/opencode-web-update` and the install
command in `index.html`.

## Security

- Proxy listens on `127.0.0.1` only.
- A random `OPENCODE_SERVER_PASSWORD` is generated per launch unless you set
  one. The browser connects to the proxy; the proxy authenticates upstream.
- Do not expose the proxy port to your network. Do not remove the localhost
  bind.

## License

MIT for the skin, studio, proxy and launchers in this folder. opencode itself
is licensed by its authors and is installed separately.
