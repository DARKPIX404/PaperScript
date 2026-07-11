# Contributing

Thanks for your interest in PaperScript.

## Setup
- JDK 21 and Node.js LTS (Gradle via the wrapper).
- `./gradlew :host:shadowJar :host-legacy:build`
- `cd sdk && npm install && npm run build`

## Rules
- The Java facade (`host/.../api/`) is the runtime truth; `sdk/src/global.ts` must mirror it. Update both and bump `apiVersion` on breaking changes.
- Never expose raw Java to scripts; add capabilities only through the curated facade.
- Keep the sandbox flags (no `Java.type`, no IO/native/threads) and touch GraalJS contexts only from the server main thread.
- Stable versions only; record bumps in `host/build.gradle.kts` / `sdk/package.json`.

## Pull requests
- Small, focused changes. Include a short note on what you tested (build, smoke, or server run).
- CI must be green (`.github/workflows/ci.yml`).
