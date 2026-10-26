# SHΞN ʜᴇʀᴏ™ Termux Agent Coder

**SHΞN ʜᴇʀᴏ™** brings the familiar OpenCode coding workspace to Android through Termux, with a locally hosted interface and API.

The original coding UI and its workflows are preserved. The home screen carries SHΞN ʜᴇʀᴏ™ branding, while the local engine runs on the device and the proxy binds to loopback.

## Install on Termux

```bash
curl -fsSL https://raw.githubusercontent.com/aishervin/SHENhero/main/termux-skin/install.sh | bash
```

The installer prepares the local launcher and UI, then starts the local service. The UI and API remain on the device; no SHΞN ʜᴇʀᴏ™ hosted backend is required.

## Commands

```bash
opencode-web-open          # start the local coding workspace
opencode-web-open ~/code   # open a project directory
opencode-web-stop          # stop local processes
opencode-web-update        # update the local UI and launcher
```

The local API endpoint used by compatible Android apps is:

```
http://127.0.0.1:4096
```

## Repository

- Source and releases: https://github.com/aishervin/SHENhero
- Issues and feature requests: https://github.com/aishervin/SHENhero/issues

## Upstream notices

This project customizes and packages the OpenCode UI and engine for Termux. OpenCode remains a separate upstream project; applicable third-party copyright notices and MIT license terms are retained in the source distribution. SHΞN ʜᴇʀᴏ™ is an independent community customization and is not an official OpenCode release.
