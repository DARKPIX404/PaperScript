# PaperScript Legacy line (1.12.2–1.16.5) — design & roadmap

The modern host (`host/`) targets Paper 1.18–1.21 on Java 17/21 with **GraalJS**.
Older servers (1.12.2 / 1.16.5) run on **Java 8 / 11**, where GraalJS cannot run
(it requires Java 17+). The legacy line therefore uses a different engine.

## Engine: Nashorn
- `javax.script` / **Nashorn** is bundled with Java 8–14, so it is available on
  1.12.2 (Java 8) and 1.16.5 (Java 11) servers with zero extra dependencies.
- Language level: ES5.1 + partial ES6. Plugin authors target the same `ps.*`
  facade; TS is still bundled to plain JS by the shared `sdk` (esbuild target
  will be lowered for the legacy line, e.g. `es2015`).

## Chat: legacy color codes
No Adventure/MiniMessage on these versions. The facade renders message strings
through `org.bukkit.ChatColor.translateAlternateColorCodes('&', text)` and the
legacy `ChatColor` enum. Authors use `&`-codes on legacy, MiniMessage on modern;
the facade abstracts the difference where feasible (documented per-call).

## Shared facade contract
Both engines expose the same `ps.*` surface (logger/events/commands/scheduler/
players/worlds/server/loc/storage). Engine-specific binding code lives in each
host; the `sdk` types are shared. API members that have no legacy equivalent are
marked and degrade gracefully (documented).

## API caveats (legacy)
- `org.bukkit.attribute.Attribute` uses `GENERIC_*` names.
- No `Component`/Adventure; `sendMessage(String)` with legacy colors.
- `World#setSpawnLocation(Location)` is available since 1.13; on 1.12.2 use the
  `setSpawnLocation(int, int, int)` overload.

## Status
- v1: `host-legacy/` was a compilable **foundation stub** (Bukkit 1.12.2, Java 8)
  plus this design.
- **v1.1 (done): Nashorn `ScriptEngine` (`--language=es6 --no-java` + prelude),
  legacy `&`-chat renderer, full shared facade, `/ps` command, tiny dependency-free
  JSON (manifest + storage). CI job `E2E legacy` boots a real Paper 1.12.2 server
  on Temurin 8 and asserts the probe markers. Bundle for legacy with
  `"target": "es2015"` in `plugin.json` (SDK ≥ 0.2.0).**
