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


## Current status (2026-07)

- Repo: `github.com/DARKPIX404/PaperScript` (public, MIT). Default branch `main`, CI green.
- Release: `v0.1.0` published with `paperscript-host-0.1.0.jar` (Shadow, ~35 MB, GraalJS shaded).
- Sandbox (already strong — do NOT weaken): `HostAccess.SCOPED`, `allowHostClassLookup(false)`, `allowIO(NONE)`, `allowNativeAccess(false)`, `allowCreateThread(false)`, `ResourceLimits` 50M statements, contexts main-thread only. See `ScriptEngine.java`.
- Hot reload is REAL: `ScriptLoader.reload(name)` + `/ps list|reload|info` (`PaperScriptCommand.java`). Per-script load errors are isolated (`loadOne` try/catch).
- Facade surface: `ScriptApi` (logger/events/commands/scheduler/players/worlds/server/loc/storage). `sdk/src/global.ts` must mirror it.
- Site: `darkpix.ru/paperscript/` live (Next export on GH Pages); card in «Проекты». Repo in `Web/darkpix.ru`.

## Runbook (commands I actually use)

- Host build (JDK 21):
  `JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot" PATH="$JAVA_HOME/bin:$PATH" ./gradlew :host:shadowJar :host-legacy:build --no-daemon`
  Jar → `host/build/libs/paperscript-host-*.jar`.
- SDK: `cd sdk && npm ci && npm run build` (tsc → `dist/`). Examples: `cd examples/<x> && npm ci && npm run build` (esbuild iife).
- Site: `cd C:/Programming/Web/darkpix.ru && npm install && npm run build` (`eslint . && next build` → `out/`). `bun` НЕ установлен — используем npm.
- Release: `git tag -a vX.Y.Z -m "…" && git push origin vX.Y.Z` → `release.yml` собирает и прикрепляет jar. Push в `main` → `ci.yml`.
- Auth для push/API: `gh` НЕ установлен, SSH нет, helper пуст → только HTTPS + PAT. Никогда не писать токен в файлы/логи/.git; использовать `GIT_ASKPASS` + env `GH_USERNAME`/`GH_PASSWORD` (скрипт печатает переменные, не значения); remotes оставлять чистыми; после публикации — ротировать токен.
- e2e (когда будет добавлен в A1): `bash e2e/run.sh` — качает Paper, стартует сервер, проверяет лог-маркер.
- SDK publish (когда готов): `cd sdk && NPM_TOKEN=… npm publish` (scoped → нужен `publishConfig.access=public`).

## Fix log — do NOT re-debug

Симптом → причина → фикс → коммит.

- **`./gradlew: Permission denied` (exit 126) в CI.** Windows git не хранит executable-бит → на Linux `gradlew` не +x. Фикс: `git update-index --chmod=+x gradlew` (режим `100755`). Коммит `2aa2e21`. Проверка: `git ls-files -s gradlew` → `100755`.
- **CI падает с 0 jobs, имя workflow = путь `.github/workflows/ci.yml` вместо `CI`.** GitHub не парсил `on:` с inline flow `branches: [main]` (unquoted `on` + flow sequence). Фикс: `"on"` в кавычках, блочные `branches: - main`, добавить `permissions: contents: read`. Коммит `035953d`. Правило: workflow YAML → `on` всегда в кавычках, списки блочные, `permissions` явные.
- **Shadow jar не содержит GraalJS polyglot-provider → движок падает в рантайме.** Причина: кастомный `configurations = emptyList()` + ручной `from(zipTree)` отключает `mergeServiceFiles()`. Не обходить Shadow — используем `shadowJar { mergeServiceFiles() }` и конкретные `org.graalvm.js:js-language` / `org.graalvm.truffle:truffle-runtime` / `polyglot` с exclude `truffle-enterprise`.
- **`npm install` падает `ECONNRESET`.** Сетевой сбой. Ретрай: `npm install --fetch-retries=6 --fetch-retry-maxtimeout=120000 --network-concurrency=2`.
- **husky pre-commit в `Web/darkpix.ru` зовёт `bun` (не установлен).** Коммитить с `--no-verify`, предварительно подтвердив lint через `npm run build`.
- **«Обрезка» мобильной `/paperscript`.** Не баг, а артефакт headless: viewport-пол 500px + `whileInView` прозрачны на полном скрине без скролла. Проверять мобильную через CDP `Emulation.setDeviceMetricsOverride` (настоящий 390×844), НЕ `--window-size`. Добавлен защитный `overflow-x-hidden`/`max-w-[100vw]` на `<main>`.
- **Git identity не задан глобально.** Для коммитов ставить локально: `user.name=DARKPIX404`, `user.email=DARKPIX404@users.noreply.github.com`.
- **Resource-limit в GraalJS 24.x — это НЕ отдельный класс.** Нет `org.graalvm.polyglot.ResourceLimitException`; исчерпание лимита приходит как `PolyglotException` с `isResourceExhausted()==true`. Детектить именно так (`Errors.java`).
- **Source maps включаются в ДВУХ местах SDK:** `sdk/src/build.ts` (programmatic `buildPlugin`) И `sdk/src/cli.ts` (`baseOpts`, путь `paperscript build`, которым пользуются примеры). CLI свой esbuild-конфиг, через `buildPlugin` не идёт.
- **e2e:** `e2e/run.sh` должен быть исполняемым в git (`git update-index --chmod=+x e2e/run.sh`, как `gradlew`); шаг в `ci.yml` вызывает `bash e2e/run.sh host/build/libs/paperscript-host-*.jar`.

## Roadmap — next improvements (точка возобновления)

Текущий выбор пользователя: **A (production-trust) + C (security/DX)**. Делать в этом порядке; публикацию в npm — в конце, отдельным токеном.

- **A1. e2e в CI (real Paper).** Создать `e2e/` (скрипт с `plugin.json` + `index.js`: лог-маркер `PS_E2E_READY` на enable, команда `psping` → `PS_E2E_PONG`) и `e2e/run.sh` (PaperMC API v2 → скачать `paper-1.21.1-<build>.jar`, `eula=true`, положить host jar в `plugins/`, скрипт в `plugins/PaperScript/scripts/e2e/`, старт в фоне, ждать `Done`, послать `psping` через stdin, grep `PS_E2E_PONG`, `stop`). Вшить шаг в job `java` в `ci.yml` после сборки jar.
- **A2. Публикация `@paperscript/sdk`.** В `sdk/package.json` добавить `publishConfig:{ "access":"public" }`, `repository`/`homepage`/`bugs`, `prepublishOnly:"npm run build"`. `npm publish` (нужен `NPM_TOKEN` в момент вызова).
- **A3. Source maps + error UX.** В `sdk/src/build.ts` включить `sourcemap` (esbuild) и прокидывать `sourcesContent`; в `ScriptInstance`/диспетче событий ловить `PolyglotException`/`ResourceLimitException` и печатать `script:line:col` + понятное сообщение (в т.ч. «script X hit the statement limit»).
- **C. Песочница уже жёсткая → задокументировать.** Создать `docs/security.md` (что запрещено, лимит, main-thread-правило, как добавлять capability только через фасад). Опционально — opt-in «trusted» доступ для своих скриптов (отложить; по умолчанию full-deny).
- **B (v1.1). Legacy-движок 1.12.2/1.16.5 на Nashorn** в `host-legacy` (см. `docs/legacy.md`): §-чат вместо MiniMessage, тот же фасад.
- **D. Дистрибуция + доки:** Hangar (PaperMC) / Modrinth / SpigotMC; мини-сайт доков (API reference, примеры, migration), OG-обложка, copy-to-clipboard в коде.

Перед любым изменением фасада: обновить `host/.../api/` **и** `sdk/src/global.ts` вместе; при ломающем изменении — поднять `apiVersion`. После изменения workflows — помнить правило про `on`/блочные списки/`permissions` (см. Fix log).
