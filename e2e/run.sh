#!/usr/bin/env bash
# Real-Paper e2e: download Paper, boot a server with the host jar + probe script,
# run a command from console and assert log markers. Pure bash + curl + grep.
set -euo pipefail

HOST_JAR_GLOB="${1:?usage: run.sh <host-jar-glob>}"
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VER="1.21.1"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
cd "$WORK"

echo "[e2e] resolving latest Paper $VER build"
BUILD_JSON=$(curl -fsSL "https://api.papermc.io/v2/projects/paper/versions/$VER/builds")
BUILD=$(printf '%s' "$BUILD_JSON" | grep -o '"build":[0-9]*' | tail -n1 | cut -d: -f2)
if [ -z "$BUILD" ]; then echo "[e2e] could not resolve Paper build"; exit 2; fi
echo "[e2e] Paper $VER build $BUILD"
curl -fsSL -o paper.jar "https://api.papermc.io/v2/projects/paper/versions/$VER/builds/$BUILD/downloads/paper-$VER-$BUILD.jar"

echo "[e2e] setting up server in $WORK"
echo "eula=true" > eula.txt
mkdir -p plugins/PaperScript/scripts/e2e
# shellcheck disable=SC2086
cp $HOST_JAR_GLOB plugins/
cp "$E2E_DIR/plugin.json" "$E2E_DIR/index.js" plugins/PaperScript/scripts/e2e/

mkfifo stdin
( java -Xmx1G -jar paper.jar nogui < stdin > server.log 2>&1 ) &
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
