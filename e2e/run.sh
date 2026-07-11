#!/usr/bin/env bash
# Real-Paper e2e: download Paper, boot a server with the host jar + probe script,
# run a command from console and assert log markers. Pure bash + curl + python3 + grep.
# PaperMC API v2 is sunset (HTTP 410); downloads use the v3 Fill API.
set -euo pipefail

HOST_JAR_GLOB="${1:?usage: run.sh <host-jar-glob>}"
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VER="${PAPER_VERSION:-1.21.1}"
JAVA_BIN="${JAVA_BIN:-java}"

# Resolve the host jar to an ABSOLUTE path *before* we cd into the temp workdir.
# The caller (CI step) may pre-expand the glob in the repo root into a relative
# path; if we cd first, that relative path resolves against $WORK and fails.
HOST_JAR_MATCH="$(ls $HOST_JAR_GLOB 2>/dev/null | head -n1 || true)"
if [ -z "$HOST_JAR_MATCH" ]; then echo "[e2e] host jar not found: $HOST_JAR_GLOB"; exit 2; fi
HOST_JAR_ABS="$(cd "$(dirname "$HOST_JAR_MATCH")" && pwd)/$(basename "$HOST_JAR_MATCH")"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
cd "$WORK"

echo "[e2e] resolving latest Paper $VER build (PaperMC v3 / fill.papermc.io)"
read -r BUILD JAR_URL JAR_NAME < <(python3 - "$VER" <<'PY'
import sys, json, urllib.request
ver = sys.argv[1]
url = "https://fill.papermc.io/v3/projects/paper/versions/%s/builds" % ver
req = urllib.request.Request(url, headers={"User-Agent": "PaperScript-e2e/1.0"})
builds = json.load(urllib.request.urlopen(req, timeout=60))
stable = [b for b in builds if b.get("channel") == "STABLE"]
pool = stable if stable else builds
b = max(pool, key=lambda x: x["id"])
d = b["downloads"]["server:default"]
print(b["id"], d["url"], d["name"])
PY
)
if [ -z "${BUILD:-}" ] || [ -z "${JAR_URL:-}" ]; then echo "[e2e] could not resolve Paper build"; exit 2; fi
echo "[e2e] Paper $VER build $BUILD -> $JAR_NAME"
curl -fsSL -o paper.jar "$JAR_URL"

echo "[e2e] setting up server in $WORK"
echo "eula=true" > eula.txt
# Data folder follows the plugin name (PaperScript modern / PaperScriptLegacy).
PS_PLUGIN_DIR="${PS_PLUGIN_DIR:-PaperScript}"
mkdir -p "plugins/$PS_PLUGIN_DIR/scripts/e2e"
cp "$HOST_JAR_ABS" plugins/
cp "$E2E_DIR/plugin.json" "$E2E_DIR/index.js" "plugins/$PS_PLUGIN_DIR/scripts/e2e/"

if ! ls plugins/paperscript-*.jar >/dev/null 2>&1; then
  echo "[e2e] host jar missing after copy (from $HOST_JAR_ABS)"
  exit 2
fi

mkfifo stdin
( "$JAVA_BIN" -Xmx1G -jar paper.jar nogui < stdin > server.log 2>&1 ) &
SVPID=$!
exec 3> stdin

READY=0
for _ in $(seq 1 150); do
  if grep -q "Done (" server.log; then READY=1; break; fi
  sleep 2
done
if [ "$READY" -ne 1 ]; then
  echo "[e2e] server did not finish startup"
  tail -n 80 server.log
  echo "stop" >&3; wait "$SVPID" || true
  exit 3
fi

sleep 3
echo "psping" >&3
sleep 3
echo "stop" >&3
wait "$SVPID" || true
exec 3>&-

echo "[e2e] ---- server.log tail ----"
tail -n 40 server.log

if grep -q "PS_E2E_READY" server.log && grep -q "PS_E2E_PONG" server.log; then
  echo "[e2e] PASS"
  exit 0
fi
echo "[e2e] FAIL: markers not found"
exit 1
