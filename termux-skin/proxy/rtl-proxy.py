#!/usr/bin/env python3
import asyncio
import base64
import os
import sys

LISTEN_HOST = os.environ.get("OC_PROXY_HOST", "127.0.0.1")
LISTEN_PORT = int(os.environ.get("OC_PROXY_PORT", "0"))
UP_HOST = os.environ.get("OC_UPSTREAM_HOST", "127.0.0.1")
UP_PORT = int(os.environ.get("OC_UPSTREAM_PORT", "4096"))
USER = os.environ.get("OPENCODE_SERVER_USERNAME", "opencode")
PW = os.environ.get("OPENCODE_SERVER_PASSWORD", "")
SKIN_DIR = os.environ.get("OC_SKIN_DIR") or os.path.expanduser("~/.opencode-web/skin")
AUTH = None
if PW:
    AUTH = "Basic " + base64.b64encode((USER + ":" + PW).encode()).decode()

FIX_CSS = r"""
html body, html body * { -webkit-font-smoothing: antialiased; }
.oc-rtl-fixed {
  unicode-bidi: plaintext !important;
  text-align: start !important;
  letter-spacing: normal !important;
  word-spacing: normal !important;
  font-feature-settings: normal !important;
  font-variant-ligatures: normal !important;
  font-family: "Inter", "Noto Sans Arabic", "Noto Naskh Arabic", "Noto Sans", Tahoma, sans-serif !important;
}
.oc-rtl-fixed pre, .oc-rtl-fixed code, .oc-rtl-fixed kbd, .oc-rtl-fixed samp {
  direction: ltr !important;
  unicode-bidi: isolate !important;
  text-align: left !important;
}
.oc-rtl-fixed .oc-rtl-fixed { font-family: inherit !important; }
textarea, input, [contenteditable="true"], [contenteditable=""] {
  letter-spacing: normal !important;
  word-spacing: normal !important;
}
textarea[dir="auto"], input[dir="auto"], [contenteditable][dir="auto"] {
  font-family: "Inter", "Noto Sans Arabic", "Noto Naskh Arabic", "Noto Sans", Tahoma, sans-serif !important;
}
"""

FIX_JS = r"""
(function () {
  "use strict";
  var AR = /[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF\uFB50-\uFDFF\uFE70-\uFEFF]/;
  var BLOCK = /^(P|LI|H1|H2|H3|H4|H5|H6|DIV|BLOCKQUOTE|TD|TH|DD|DT|FIGCAPTION|ARTICLE|SECTION|ADDRESS|MAIN|HEADER|FOOTER|SUMMARY)$/;
  var SKIP = "pre,code,kbd,samp,script,style,textarea,[data-component=\"markdown-code\"],[data-slot=\"code\"]";

  function blockFor(node) {
    var el = node.nodeType === 1 ? node : node.parentElement;
    while (el && el !== document.body && !BLOCK.test(el.tagName)) el = el.parentElement;
    return el || (node.nodeType === 1 ? node : node.parentElement);
  }

  function field(el) {
    return el && (el.tagName === "TEXTAREA" || el.tagName === "INPUT" || el.isContentEditable);
  }

  function markBlock(node) {
    var p = node.parentElement;
    if (!p || p.closest(SKIP)) return;
    var b = blockFor(node);
    if (!b || b.closest(SKIP)) return;
    if (!b.classList.contains("oc-rtl-fixed")) b.classList.add("oc-rtl-fixed");
    if (b.getAttribute("dir") !== "auto") b.setAttribute("dir", "auto");
  }

  function walk(node) {
    if (node.nodeType === 3) {
      if (node.nodeValue && AR.test(node.nodeValue)) markBlock(node);
      return;
    }
    if (node.nodeType !== 1) return;
    if (field(node) && node.getAttribute("dir") !== "auto") node.setAttribute("dir", "auto");
    if (!node.querySelectorAll) return;
    var tw = document.createTreeWalker(node, NodeFilter.SHOW_TEXT, null);
    var n;
    while ((n = tw.nextNode())) {
      if (n.nodeValue && AR.test(n.nodeValue)) markBlock(n);
    }
  }

  function scan() {
    if (document.body) walk(document.body);
  }

  document.addEventListener("DOMContentLoaded", function () {
    scan();
    var mo = new MutationObserver(function (muts) {
      for (var i = 0; i < muts.length; i++) {
        var m = muts[i];
        if (m.type === "attributes") {
          var el = m.target;
          if ((field(el) || el.classList.contains("oc-rtl-fixed")) && el.getAttribute("dir") !== "auto") {
            el.setAttribute("dir", "auto");
          }
        } else if (m.type === "characterData") {
          if (m.target.nodeValue && AR.test(m.target.nodeValue)) markBlock(m.target);
        } else {
          for (var j = 0; j < m.addedNodes.length; j++) walk(m.addedNodes[j]);
        }
      }
    });
    mo.observe(document.body, { subtree: true, childList: true, characterData: true, attributes: true, attributeFilter: ["dir"] });
    document.addEventListener("input", function (e) {
      var t = e.target;
      if (field(t)) {
        if (t.getAttribute("dir") !== "auto") t.setAttribute("dir", "auto");
        if (AR.test(t.value || "")) t.classList.add("oc-rtl-field");
        else t.classList.remove("oc-rtl-field");
      }
    }, true);
  });
})();
"""

INJECT = (
    '<style id="oc-rtl-style">' + FIX_CSS + '</style>'
    '<link rel="stylesheet" href="/__oc_skin.css">'
    '<script src="/__oc_rtl_fix.js"></script>'
    '<script src="/__oc_skin.js"></script>'
)
FIX_BYTES = (FIX_JS + "\n").encode()
SKIN_FILES = {
    b"/__oc_skin.css": ("skin.css", b"text/css; charset=utf-8"),
    b"/__oc_skin.js": ("skin.js", b"application/javascript; charset=utf-8"),
}


def hget(headers, name):
    name = name.lower()
    for k, v in headers:
        if k.lower() == name:
            return v
    return None


def parse_head(head):
    lines = head.split(b"\r\n")
    start = lines[0]
    headers = []
    for line in lines[1:]:
        if not line:
            continue
        k, sep, v = line.partition(b":")
        if sep:
            headers.append((k.strip(), v.strip()))
    return start, headers


def build_request(start, headers, websocket):
    out = []
    have_auth = False
    for k, v in headers:
        lk = k.lower()
        if lk == b"host":
            continue
        if lk == b"authorization":
            if AUTH:
                out.append((b"authorization", AUTH.encode()))
            have_auth = True
            continue
        if lk == b"accept-encoding":
            continue
        if not websocket and lk in (b"connection", b"proxy-connection", b"keep-alive"):
            continue
        out.append((k, v))
    out.append((b"host", ("%s:%d" % (UP_HOST, UP_PORT)).encode()))
    if AUTH and not have_auth:
        out.append((b"authorization", AUTH.encode()))
    if not websocket:
        out.append((b"connection", b"close"))
    head = start + b"\r\n" + b"\r\n".join(k + b": " + v for k, v in out) + b"\r\n\r\n"
    return head


def build_response(start, headers, length=None):
    out = []
    for k, v in headers:
        lk = k.lower()
        if lk in (b"connection", b"keep-alive", b"proxy-connection"):
            continue
        if lk in (b"transfer-encoding", b"content-length", b"content-encoding"):
            if length is None and lk in (b"transfer-encoding", b"content-length"):
                out.append((k, v))
                continue
            continue
        out.append((k, v))
    if length is not None:
        out.append((b"content-length", str(length).encode()))
    out.append((b"connection", b"close"))
    return start + b"\r\n" + b"\r\n".join(k + b": " + v for k, v in out) + b"\r\n\r\n"


async def read_head(reader):
    buf = b""
    while True:
        chunk = await reader.read(65536)
        if not chunk:
            return None, None
        buf += chunk
        i = buf.find(b"\r\n\r\n")
        if i >= 0:
            return buf[: i + 4], buf[i + 4 :]


async def dechunk(reader, initial):
    buf = initial
    out = bytearray()
    while True:
        while b"\r\n" not in buf:
            chunk = await reader.read(65536)
            if not chunk:
                return bytes(out)
            buf += chunk
        line, _, buf = buf.partition(b"\r\n")
        try:
            size = int(line.split(b";")[0], 16)
        except ValueError:
            return bytes(out)
        if size == 0:
            return bytes(out)
        while len(buf) < size + 2:
            chunk = await reader.read(65536)
            if not chunk:
                return bytes(out)
            buf += chunk
        out += buf[:size]
        buf = buf[size + 2 :]


async def stream_body(reader, writer, rest, headers):
    te = (hget(headers, b"transfer-encoding") or b"").lower()
    cl = hget(headers, b"content-length")
    tail = b""
    if b"chunked" in te:
        buf = rest
        while True:
            if buf:
                writer.write(buf)
                await writer.drain()
                tail = (tail + buf)[-7:]
                buf = b""
            if tail.endswith(b"0\r\n\r\n"):
                return
            buf = await reader.read(65536)
            if not buf:
                return
    elif cl is not None:
        try:
            remaining = int(cl)
        except ValueError:
            remaining = None
        if remaining is None:
            while True:
                chunk = await reader.read(65536)
                if not chunk:
                    return
                writer.write(chunk)
                await writer.drain()
        if rest:
            writer.write(rest)
            remaining -= len(rest)
        while remaining > 0:
            chunk = await reader.read(min(65536, remaining))
            if not chunk:
                return
            writer.write(chunk)
            await writer.drain()
            remaining -= len(chunk)
    else:
        if rest:
            writer.write(rest)
            await writer.drain()
        while True:
            chunk = await reader.read(65536)
            if not chunk:
                return
            writer.write(chunk)
            await writer.drain()


async def pump_body(reader, writer, initial, headers):
    te = (hget(headers, b"transfer-encoding") or b"").lower()
    cl = hget(headers, b"content-length")
    try:
        if cl is not None:
            try:
                remaining = int(cl)
            except ValueError:
                return
            data = initial[:remaining]
            if data:
                writer.write(data)
                remaining -= len(data)
            while remaining > 0:
                chunk = await reader.read(min(65536, remaining))
                if not chunk:
                    return
                writer.write(chunk)
                remaining -= len(chunk)
        elif b"chunked" in te:
            buf = initial
            tail = b""
            while True:
                if buf:
                    writer.write(buf)
                    await writer.drain()
                    tail = (tail + buf)[-7:]
                    buf = b""
                if tail.endswith(b"0\r\n\r\n"):
                    return
                buf = await reader.read(65536)
                if not buf:
                    return
        elif initial:
            writer.write(initial)
        await writer.drain()
    except Exception:
        return


async def relay(reader, writer):
    try:
        while True:
            data = await reader.read(65536)
            if not data:
                break
            writer.write(data)
            await writer.drain()
    except Exception:
        pass
    finally:
        try:
            writer.close()
        except Exception:
            pass


async def handle(client_reader, client_writer):
    up_writer = None
    try:
        head, rest = await read_head(client_reader)
        if not head:
            return
        start, headers = parse_head(head)
        upgrade = hget(headers, b"upgrade") or b""
        websocket = b"websocket" in upgrade.lower()

        try:
            path = start.split(b" ")[1]
        except IndexError:
            path = b"/"

        skin = SKIN_FILES.get(path.split(b"?")[0])
        if skin is not None:
            name, ctype = skin
            try:
                with open(os.path.join(SKIN_DIR, name), "rb") as fh:
                    data = fh.read()
            except OSError:
                data = b""
            if start.startswith(b"HEAD"):
                client_writer.write(b"HTTP/1.1 200 OK\r\nContent-Type: " + ctype + b"\r\nContent-Length: 0\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n")
            else:
                client_writer.write(b"HTTP/1.1 200 OK\r\nContent-Type: " + ctype + b"\r\nCache-Control: no-store\r\nContent-Length: " + str(len(data)).encode() + b"\r\nConnection: close\r\n\r\n")
                client_writer.write(data)
            await client_writer.drain()
            return

        if path.split(b"?")[0] == b"/__oc_rtl_fix.js":
            if start.startswith(b"HEAD"):
                client_writer.write(b"HTTP/1.1 200 OK\r\nContent-Type: application/javascript; charset=utf-8\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")
            else:
                client_writer.write(b"HTTP/1.1 200 OK\r\nContent-Type: application/javascript; charset=utf-8\r\nCache-Control: no-store\r\nContent-Length: " + str(len(FIX_BYTES)).encode() + b"\r\nConnection: close\r\n\r\n")
                client_writer.write(FIX_BYTES)
            await client_writer.drain()
            return

        up_reader, up_writer = await asyncio.open_connection(UP_HOST, UP_PORT)
        req = build_request(start, headers, websocket)
        up_writer.write(req)

        if websocket:
            if rest:
                up_writer.write(rest)
            await up_writer.drain()
            await asyncio.gather(
                relay(client_reader, up_writer),
                relay(up_reader, client_writer),
            )
            return

        asyncio.create_task(pump_body(client_reader, up_writer, rest, headers))

        resp_head, resp_rest = await read_head(up_reader)
        if not resp_head:
            return
        status, resp_headers = parse_head(resp_head)
        ctype = (hget(resp_headers, b"content-type") or b"").lower()
        inject = ctype.startswith(b"text/html")

        if inject:
            body = await dechunk(up_reader, resp_rest) if b"chunked" in (hget(resp_headers, b"transfer-encoding") or b"").lower() else await stream_all(up_reader, resp_rest, resp_headers)
            marker = b"</head>"
            if marker in body:
                body = body.replace(marker, INJECT.encode() + marker, 1)
            else:
                body = INJECT.encode() + body
            client_writer.write(build_response(status, resp_headers, len(body)))
            client_writer.write(body)
            await client_writer.drain()
        else:
            client_writer.write(build_response(status, resp_headers, None))
            await client_writer.drain()
            await stream_body(up_reader, client_writer, resp_rest, resp_headers)
    except Exception:
        pass
    finally:
        if up_writer is not None:
            try:
                up_writer.close()
            except Exception:
                pass
        try:
            client_writer.close()
        except Exception:
            pass


async def stream_all(reader, rest, headers):
    cl = hget(headers, b"content-length")
    if cl is not None:
        try:
            n = int(cl)
        except ValueError:
            n = None
        data = bytearray(rest)
        if n is not None:
            while len(data) < n:
                chunk = await reader.read(65536)
                if not chunk:
                    break
                data += chunk
            return bytes(data[:n])
    data = bytearray(rest)
    while True:
        chunk = await reader.read(65536)
        if not chunk:
            return bytes(data)
        data += chunk


async def main():
    server = await asyncio.start_server(handle, LISTEN_HOST, LISTEN_PORT)
    port = server.sockets[0].getsockname()[1]
    sys.stdout.write("%s\n" % port)
    sys.stdout.flush()
    async with server:
        await server.serve_forever()


if __name__ == "__main__":
    asyncio.run(main())
