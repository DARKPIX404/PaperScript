# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/) and the project uses SemVer.

## [0.1.0] — initial
- Modern host (`host/`): Paper 1.18–1.21, Java 17/21, GraalJS, sandboxed contexts, hot reload, `/ps list|reload|info`.
- Typed facade `ps` (logger/events/commands/scheduler/players/worlds/server/loc/storage) with MiniMessage chat.
- `@paperscript/sdk`: types + esbuild CLI (`init/build/dev`).
- Examples: `hello`, `essentials`.
- Legacy foundation (`host-legacy/`, docs/legacy.md) for 1.12.2–1.16.5 (Nashorn engine in v1.1).
