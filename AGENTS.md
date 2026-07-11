# AGENTS.md (PaperScript)

Local rules for this project. They extend the root `C:/Programming/AGENTS.md`.

## Layout

- `host/` — Java host plugin (Gradle Kotlin DSL, Shadow). Package `dev.paperscript.host`.
  - `api/` — curated facade exposed to guests as the global `ps`.
  - `runtime/` — GraalJS contexts, manifest, loader, event bridge.
  - `command/` — the `/ps` management command.
- `sdk/` — npm package `@paperscript/sdk` (types + esbuild CLI).
- `examples/` — sample plugins.

## Build

- Host: `cd host && ./gradlew shadowJar` (JDK 21).
- SDK: `cd sdk && npm install && npm run build` (Node LTS).
- Example: `cd examples/hello && npm install && npm run build`.

## Conventions

- The Java facade (`host/.../api/`) is the **runtime truth**; `sdk/src/global.ts` must **mirror** it. When you add or change a `ps.*` capability, update both sides and bump `apiVersion` if it is a breaking change.
- Never expose raw Java to guests. Keep `allowHostClassLookup(false)`, `allowIO(false)`, `allowNativeAccess(false)`, `allowCreateThread(false)`. Add new capabilities only through the curated facade.
- GraalJS contexts are single-threaded: touch a context only from the server main thread (events and main-thread scheduler tasks). Never use a `Context`/`Value` from async threads.
- Guest-facing Java types are slim wrappers (`ScriptPlayer`, `ScriptWorld`, ...), not raw Bukkit objects.
- Bean getters (`getX` / `isX`) become guest properties `x`; keep names consistent with `global.ts`.
- Never bypass Shadow's configuration merge (`configurations = emptyList()` + manual `from(zipTree)`): it disables `mergeServiceFiles()`, which drops the GraalJS polyglot-provider service file and breaks the engine at runtime.
- Depend on the concrete community jars (`org.graalvm.js:js-language`, `org.graalvm.truffle:truffle-runtime`, `org.graalvm.polyglot:polyglot`) and keep `truffle-enterprise` excluded. The `org.graalvm.js:js` aggregator is POM-only and pulls the EE runtime that fails on a stock JDK.
- GraalJS runs interpreted on a stock JDK; for JIT, use `-XX:+EnableJVMCI` on a JVMCI-enabled JDK.
- Stable versions only; bump deliberately and record the change in `host/build.gradle.kts` / `sdk/package.json`.

## Release & versioning

- SemVer. Tag `v*` triggers `.github/workflows/release.yml` (builds and attaches `paperscript-host-*.jar` to a GitHub Release).
- Keep the Java facade (`host/.../api/`) and `sdk/src/global.ts` in sync; bump `apiVersion` on breaking changes.
- Multi-module: `:host` (modern 1.18–1.21, GraalJS) and `:host-legacy` (1.12.2–1.16.5, Nashorn — engine in v1.1). See `docs/legacy.md`.
