# Security model

PaperScript runs guest JS/TS inside a hardened GraalJS context. The goal: a script
can use the full curated server API, but cannot escape the sandbox or destabilise
the host.

## What guest code cannot do

- `Java.type` / host class lookup — disabled (`allowHostClassLookup(false)`).
- File / network / process IO — disabled (`allowIO(IOAccess.NONE)`). All data
  access goes through the facade (`ps.storage`, `ps.players`, `ps.worlds`, …).
- Native / unsafe access — disabled (`allowNativeAccess(false)`).
- Thread creation — disabled (`allowCreateThread(false)`).
- Only the curated facade (`ps`) is reachable: `HostAccess.ALL` exposes the
  facade's public members, while raw-Bukkit accessors on wrappers
  (`ScriptPlayer.handle()`, `ScriptLocation.of()/resolve()`) are kept
  package-private so guests can never reach raw Bukkit objects.

## Legacy line (1.12.2–1.16.5, Nashorn)

The legacy host (`host-legacy/`) enforces the same contract with different
mechanics — and one honest gap:

- Nashorn engines are created with `--no-java` (no `Java.type`/`Packages`/class
  lookup) plus a prelude that deletes the remaining host globals
  (`load`, `quit`, `$EXEC`, `$ENV`, `readFully`, …).
- Same rule as modern: only the `ps` facade is bound; wrappers expose no raw
  Bukkit (package-private accessors).
- **No statement/time limit** — Nashorn has no equivalent of GraalJS
  `ResourceLimits`. A runaway `while(true)` script can stall the server main
  thread. Only run scripts you trust on legacy servers; prefer the modern host
  where hard limits exist.
- `ctx.args` is a Java array: index access and `.length` work, JS Array methods
  do not — use `ctx.arg(i)` or `Array.prototype.slice.call(ctx.args)`.

## Runaway protection

- A statement limit (`ResourceLimits`, 50 000 000) aborts a script that loops
  forever; the host logs `script '<name>' hit the statement limit` and isolates
  the failure to that script.
- Per-script load and per-event/per-command dispatch errors are caught and
  isolated — one broken script does not take down others or the server.

## Threading

GraalJS contexts are single-threaded. The host touches a context only from the
server main thread (enable/disable, events, main-thread scheduler tasks). Never
use a `Context` / `Value` from async threads.

## Adding capabilities

Add features ONLY through the curated facade (`host/.../api/`) and mirror them in
`sdk/src/global.ts`. Never re-enable host class lookup, IO, native access or
thread creation globally. If a trusted script genuinely needs more, add a narrow,
documented facade method — not a sandbox escape hatch.
