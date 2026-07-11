# host-legacy (PaperScript for 1.12.2–1.16.5)

Status: **v1.1 — Nashorn engine + full `ps.*` facade** (see `docs/legacy.md`).
Verified in CI against a real Paper 1.12.2 server (job `E2E legacy`).

- Target: Bukkit/Spigot/Paper 1.12.2–1.16.5 on **Java 8/11** (compiles with
  `options.release = 8`; the jar runs on any Java 8-14 runtime).
- Engine: **Nashorn** (`javax.script`, bundled in Java 8-14) — GraalJS needs
  Java 17+ and cannot run here. Engines are created via reflection with
  `--language=es6 --no-java` (ES6 syntax, no `Java.type`/class lookup) plus a
  prelude that deletes the remaining host globals.
- Chat: legacy `§` color codes via `ChatColor.translateAlternateColorCodes('&', …)`
  (no Adventure/MiniMessage on these versions).
- Facade: full parity with the modern host (`logger/events/commands/scheduler/
  players/worlds/server/loc/storage` + `onEnable/onDisable`) — the same plugin
  scripts run on both lines. Bundle for legacy with `"target": "es2015"` in the
  plugin's `plugin.json` (SDK ≥ 0.2.0).
- Caveats: no statement/time limit on Nashorn (see `docs/security.md`);
  `ctx.args` is a Java array (index + `.length`, no JS Array methods).
