# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/) and the project uses SemVer.

## [0.2.0]
- Legacy host (`host-legacy/`) v1.1: sandboxed **Nashorn** engine
  (`--language=es6 --no-java` + host-global prelude) for Paper/Spigot
  1.12.2–1.16.5 on Java 8/11.
- Full `ps.*` facade parity on legacy (logger/events/commands/scheduler/
  players/worlds/server/loc/storage + `onEnable/onDisable`), legacy `&`-chat
  rendering, `/ps list|reload|info`, dependency-free JSON manifest/storage.
- SDK: `target` option — `"target": "es2015"` in `plugin.json` bundles for the
  legacy line (`buildPlugin(entry, out, extra, target)`; CLI reads plugin.json).
  Published as `@paperscript/sdk@0.2.0`.
- CI: `E2E legacy` job boots a real Paper 1.12.2 server on Temurin 8 and runs
  the probe; `e2e/run.sh` accepts `PAPER_VERSION`/`JAVA_BIN`/`PS_PLUGIN_DIR`.

## [0.1.0] — initial
- Modern host (`host/`): Paper 1.18–1.21, Java 17/21, GraalJS, sandboxed contexts, hot reload, `/ps list|reload|info`.
- Typed facade `ps` (logger/events/commands/scheduler/players/worlds/server/loc/storage) with MiniMessage chat.
- `@paperscript/sdk`: types + esbuild CLI (`init/build/dev`). Published to npm as `@paperscript/sdk@0.1.0` (install + `paperscript` CLI verified).
- Examples: `hello`, `essentials`.
- Legacy foundation (`host-legacy/`, docs/legacy.md) for 1.12.2–1.16.5 (Nashorn engine in v1.1).
