# PaperScript

[![CI](https://github.com/DARKPIX404/PaperScript/actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/DARKPIX404/PaperScript)](../../releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Write **Minecraft server plugins in JavaScript/TypeScript**. A host plugin embeds a JS engine and loads scripts from a folder, exposing a small typed API (`ps`) with hot reload. Site: **https://darkpix.ru/paperscript**.

> Пишите плагины для Minecraft на JS/TS. Хост-плагин встраивает JS-движок и грузит скрипты из папки, отдавая типизированный API `ps` с горячей перезагрузкой.

## Version matrix

| Minecraft | Java | Engine | Module | Status |
|-----------|------|--------|--------|--------|
| 1.18 – 1.21 | 17 / 21 | GraalJS | `host/` | ✅ supported (verified on 1.21.1) |
| 1.12.2 – 1.16.5 | 8 / 11 | Nashorn | `host-legacy/` | 🚧 v1.1 (see [`docs/legacy.md`](docs/legacy.md)) |

GraalJS requires Java 17+, so 1.12.2/1.16.5 (Java 8/11) use a separate legacy line built on Nashorn (bundled with those JDKs).

## Quick start

1. Download `paperscript-host-*.jar` from [Releases](../../releases) and drop it into your Paper server's `plugins/`.
2. Start the server once — it creates `plugins/PaperScript/scripts/`.
3. Put a script folder (with `plugin.json` + `dist/index.js`) into `scripts/`, then:
   ```
   /ps list
   /ps reload <name>
   ```

## Write a plugin (TypeScript)

```bash
cd sdk && npm install && npm run build   # build the CLI once
npx paperscript init my-plugin
cd my-plugin && npm install && npm run build   # -> dist/index.js
```

```ts
ps.onEnable(() => {
  ps.logger.info("Hello!");
  ps.commands.register("hello", (ctx) => {
    ctx.sender.sendMessage("<green>Hello, " + ctx.sender.name + "!</green>");
  });
  ps.events.onPlayerJoin((e) => e.player.sendMessage("<gold>Welcome!</gold>"));
});
```

All message strings accept **MiniMessage** (`<red>`, `<gradient:..>`, `<bold>`, hover/click) on the modern line.

Deploy: copy the plugin folder (`plugin.json` + `dist/`) into `plugins/PaperScript/scripts/`. `npm run dev` watches TS; `/ps reload <name>` hot-reloads.

## The `ps` API

- `ps.onEnable(fn)` / `ps.onDisable(fn)`
- `ps.logger.info|warn|error(msg)`
- `ps.events.onPlayerJoin(cb)` / `onPlayerQuit(cb)`
- `ps.commands.register(name, handler, description?, usage?)`
- `ps.scheduler.runTask | runTaskLater | runTaskTimer | cancelTask`
- `ps.players.online()` / `get(name)`
- `ps.worlds.all()` / `get(name)` · `world.spawnLocation` / `setSpawnLocation(loc)`
- `ps.server.broadcast(msg)`
- `ps.loc.create(world, x, y, z[, yaw, pitch])`
- `ps.storage.get/set/remove/save` (JSON-string key/value, persisted)
- `Player`: `location`, `teleport(loc)`, `heal()`, `feed()`, `allowFlight`, `flying`, `gameMode/setGameMode()`, `foodLevel`

Types ship in `@paperscript/sdk` (`types: ["@paperscript/sdk"]`).

## Examples

- [`examples/hello`](examples/hello) — minimal (command + join listener).
- [`examples/essentials`](examples/essentials) — `/spawn /setspawn /tp /tpa /tpaccept /heal /feed /fly /gamemode /home /sethome /delhome` with a MiniMessage chat theme.

## Sandbox

Scripts cannot use `Java.type`, host IO, native access or threads — only the curated `ps` facade is exposed, guarded by a statement limit. GraalJS contexts are single-threaded; the host touches them only from the server main thread.

## Build from source

Requires JDK 21 and Node.js LTS (Gradle via the wrapper).

```bash
./gradlew :host:shadowJar :host-legacy:build   # jars in host/build/libs, host-legacy/build/libs
cd sdk && npm install && npm run build
cd examples/essentials && npm install && npm run build
```

## Links

- Site & docs: https://darkpix.ru/paperscript
- Issues: ../../issues
- License: [MIT](LICENSE)
