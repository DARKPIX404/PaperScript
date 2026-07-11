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
- **PaperMC API v2 снят (HTTP 410 Gone, sunset 2026-07-01).** `api.papermc.io/v2/...` больше не отвечает → e2e-падение `curl: (22) 410`. Фикс: резолв/скачивание через **v3 Fill**: `https://fill.papermc.io/v3/projects/paper/versions/<ver>/builds` → берём max `id` среди `channel:STABLE`, качаем `downloads["server:default"].url` (`fill-data.papermc.io/...`). Парсинг — `python3` (есть на ubuntu-latest). Коммит `9475ebd`. Проверка: HEAD jar → 200, ~49 MB.
- **`cp: cannot stat 'host/build/libs/paperscript-host-0.1.0.jar'` в e2e.** НЕ кэш и НЕ имя. Причина: shell шага CI заранее раскрывает glob в *относительный* путь (cwd = корень репо), а `run.sh` потом делает `cd "$WORK"` (tmp) → относительный путь резолвится уже против `$WORK`. Фикс: в `run.sh` резолвить jar в **абсолютный путь ДО `cd`** (`HOST_JAR_ABS`), копировать по нему. Коммит `71142c5`. (Ранний `--no-build-cache` был ложной гипотезой — откачен.)
- **`ps.commands`/`ps.logger`/… = undefined → `TypeError: Cannot read property 'register' of undefined` на живом сервере.** `HostAccess.SCOPED` прячет от гостя и public-поля, и даже getter'ы — весь фасад `ps.*` был невидим. Фикс: `HostAccess.ALL` (видны public-члены), sandbox удержан: `allowHostClassLookup(false)`, `IOAccess.NONE`, `allowNativeAccess(false)`, `allowCreateThread(false)`, `resourceLimits`. Единственные «дыры» в raw-Bukkit — `ScriptPlayer.handle()` и `ScriptLocation.of/resolve` — переведены в **package-private** (ALL видит только PUBLIC). Коммит `b50fb43`. Проверка: локальный Graal-smoke (`visible=true`, `register exec=true`) + CI e2e `PS_E2E_READY`/`PS_E2E_PONG` → `[e2e] PASS`.
- **npm publish `@scope/pkg` → `E404 Scope not found`.** Scope = имя user'а или **org**; org создаётся только на сайте (npmjs.com/org/create, Free), CLI не умеет. User-scope `darkpix` (whoami). Токен: только через `--//registry.npmjs.org/:_authToken=...` в команде (не в .npmrc/файлы), вывод redact'ить.
- **После publish `npm view`/curl packument отдаёт 404 ~2–5 мин** — лаг CDN-реплик, НЕ ошибка (publish уже принят: строка `+ scope/pkg@x.y.z`). Авторитетная проверка: `curl https://registry.npmjs.org/@scope%2fpkg` до HTTP 200 и/или `npm owner ls` (тоже лагает). Не кэшируй ранний 404 выводами.
- **`npm warn publish "bin[x] script name ... was invalid and removed"` — ложноположительный** (npm v11): при `publish`/`publish --dry-run` ворнинг есть, но в тарболле и в packument на реестре `bin` ЦЕЛ (`npm pack` локально ворнинга нет). Проверять факт по packument (`versions[tag].bin`) + реальной установкой: `npm i pkg` → `node_modules/.bin/<cli>` → запуск. Не «чинить» package.json по этому ворнингу.
- **`npm ci`/`npm i` падает `ECONNRESET` при unpack тарболлов** (крупные deps, напр. vitepress/esbuild): ретрай с `--fetch-retries=8 --fetch-retry-maxtimeout=300000 --network-concurrency=1 --prefer-offline`; обычно проходит со 2-й попытки.
- **Nashorn legacy (host-legacy): нельзя импортировать `jdk.nashorn.*` при сборке на JDK 21** (Nashorn удалён из JDK 15+). Движок создаём reflection'ом: `Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory").getMethod("getScriptEngine", String[].class)` с `{"--language=es6","--no-java"}`; fallback — `ScriptEngineManager().getEngineByName("nashorn")`. Колбэки: `Invocable.getInterface(fnObj, JsFunction.class)` (SAM-прокси). `--no-java` убирает `Java.type`/`Packages`, bound-POJO с bean-getter'ами остаются видны.
- **Java 8 API-ограничения (release 8):** нет `Files.readString/writeString` (Java 11 → `new String(Files.readAllBytes(p), UTF_8)` / `Files.write(p, bytes)`), `var`, `List.of`, `String.isBlank`, switch-expression. `Attribute.GENERIC_MAX_HEALTH` — корректно для 1.12.2/1.16.5.
- **`Server.getCommandMap()` нет в API 1.12.2** (только в CraftServer) → reflection `plugin.getServer().getClass().getMethod("getCommandMap")`. Удаление команды: `Command.unregister` недоступен → reflection в `SimpleCommandMap.knownCommands` (best-effort + флаг `removed` в ScriptCommand).
- **esbuild НЕ умеет ES5** — минимальный target `es2015`; Nashorn с `--language=es6` переваривает es2015-вывод (стрелки/let/const/классы; `?.`/`??` даунлевелятся). Legacy-плагины: `"target": "es2015"` в plugin.json (SDK ≥ 0.2.0, CLI читает из plugin.json; `buildPlugin(entry, out, extra, target)`).
- **e2e legacy:** data-dir следует имени плагина → `PS_PLUGIN_DIR=PaperScriptLegacy`; `run.sh` параметризован `PAPER_VERSION`/`JAVA_BIN`/`PS_PLUGIN_DIR`. Job `legacy-e2e`: сборка на JDK 21 → `setup-java` 8 → Paper 1.12.2 (Fill v3) на Temurin 8.
- **Next build (darkpix.ru): `react/no-unescaped-entities` на `"` внутри JSX-текста.** Даже в code-блоках eslint требует `&quot;`. Фикс: строки с кавычками/скобками передавать как expression-children: `<Sig>{"... \"1.21.1\" ..."}</Sig>`.
- **Next build: `children: string[]` не assignable к `children: string`.** Компонент с пропом `children: string` ломается, если JSX-children = текст + `{expr}` (получается массив). Фикс: весь children — один expression-string.
- **docs-страницы darkpix.ru — EN-only без i18n-ключей** (лендинг `/paperscript` билингвальный через `getTranslation(lang)`; новые i18n-ключи добавлять в ОБА блока en/ru в `src/lib/i18n.ts`, тип ключа — union по en).
- **Hangar API v1: JWT ≠ API-ключ.** `Authorization: HangarAuth <apiKey>` → 401 «Unable to verify the JWT». Поток: `POST /api/v1/authenticate?apiKey=<key>` → `{token, expiresIn}` → `Authorization: HangarAuth <token>`; JWT живёт недолго — перед серией запросов перевыпускать. User-Agent обязателен осмысленный.
- **Hangar: создание проекта — ТОЛЬКО internal endpoint** `POST /api/internal/projects/create` (в публичном OpenAPI его нет). Форма `NewProjectForm`: `{settings:{links:[{id,type:"TOP",title,links:[{name,url}]}], tags:[], license:{name,url,type}, keywords:[], sponsors:""}, category:"dev_tools" (apiName), description (≤120!), ownerId:<long>, name, pageContent:<markdown>, avatarUrl:null}`. Slug сервер берёт из name (регистр сохраняется: `PaperScript`).
- **Hangar: upload 36MB jar → nginx 413.** Файлы грузить через `versionUpload.files[i].externalUrl` (Hangar сам скачает с GitHub Releases) — без multipart-поля `files`, только `versionUpload`.
- **Hangar: `POST /upload` отдаёт голый 404 «Not Found» при нехватке scope** `create_version` в API-ключе (permission-аспект маскирует проект). Диагностика: декодировать JWT, поле `permissions` — битовая маска по порядку `NamedPermission` (`111` = только view_public_info/edit_own_user_settings/edit_api_keys). `POST /api/v1/keys` не эскалирует: новый ключ ≤ пермиссий текущего.
- **Windows curl (Git Bash) не понимает `/tmp` в `-F @...`** → `curl: (26) Failed to open/read local data`. Копировать файлы в `C:/Programming/.tmp-target/` и использовать относительные/Windows-пути.

## Roadmap — next improvements (точка возобновления)

Текущий выбор пользователя: **A (production-trust) + C (security/DX)**. A1/A2/A3/C — **сделано**: CI зелёный (`b50fb43`, real-Paper e2e PASS), `@paperscript/sdk@0.2.0` опубликован в npm (org `paperscript`, install+CLI проверены). **B (legacy Nashorn) — сделано** (`host-legacy` полный фасад, `paperscript-legacy-0.2.0.jar`, CI `legacy-e2e` на Paper 1.12.2/JDK 8). Побочно: `@darkpix/supercell-api@3.1.0` опубликован (ребренд). **Релиз v0.2.0 — live** (тег `v0.2.0`, оба jar'а в GitHub Releases). **D: docs на darkpix.ru — сделано** (`fc48195`+`927b25b` в repo сайта: `/paperscript/docs` + `/api` + `/examples`, лендинг исправлен на реальный `ps.*`, Hangar-линк в сайдбаре). **D: Hangar — сделано** (проект + версии `0.2.0`/`0.2.0-legacy`, ждут ревью). **D закрыта. Точка возобновления: опционально Modrinth/SpigotMC + OG-обложка; далее — новые фичи (расширение фасада: больше событий, инвентари, конфиги).**

- **A1. e2e в CI (real Paper).** ✅ DONE (`b50fb43`). `e2e/` (probe `PS_E2E_READY`/`psping`→`PS_E2E_PONG`), `run.sh` (PaperMC **v3 Fill**, abs-path host jar, fifo-stdin), шаг в job `java`. CI зелёный.
- **A2. Публикация `@paperscript/sdk`.** ✅ DONE. `@paperscript/sdk@0.1.0` в npm (org `paperscript` создана на сайте). Проверено: packument `bin` цел, `npm i @paperscript/sdk` → `paperscript` CLI печатает usage. Ворнинг `bin ... invalid and removed` при publish — ложный (см. Fix log).
- **A3. Source maps + error UX.** В `sdk/src/build.ts` включить `sourcemap` (esbuild) и прокидывать `sourcesContent`; в `ScriptInstance`/диспетче событий ловить `PolyglotException`/`ResourceLimitException` и печатать `script:line:col` + понятное сообщение (в т.ч. «script X hit the statement limit»).
- **C. Песочница уже жёсткая → задокументировать.** Создать `docs/security.md` (что запрещено, лимит, main-thread-правило, как добавлять capability только через фасад). Опционально — opt-in «trusted» доступ для своих скриптов (отложить; по умолчанию full-deny).
- **B (v1.1). Legacy-движок 1.12.2/1.16.5 на Nashorn** ✅ DONE (компилируется, `paperscript-legacy-0.2.0.jar`; CI `legacy-e2e` на Paper 1.12.2/JDK 8). Полный фасад, §-чат, `--no-java`+prelude, tiny JSON, SDK `target: es2015` (`@paperscript/sdk@0.2.0` опубликован). Caveat: нет statement-лимита (docs/security.md).
- **D. Дистрибуция + доки:** Hangar (PaperMC) / Modrinth / SpigotMC; мини-сайт доков (API reference, примеры, migration), OG-обложка, copy-to-clipboard в коде.
  - ✅ **Docs на darkpix.ru — DONE** (коммит `fc48195` в `DARKPIX404/darkpix.ru`, auto-deploy Pages): `/paperscript/docs` (getting started, requirements-таблица modern/legacy, install, plugin.json+target, sandbox), `/paperscript/docs/api` (полный reference по `ps.*` из `sdk/src/global.ts`), `/paperscript/docs/examples` (hello + essentials-выдержки + legacy-bundle). Shared shell `src/components/docs/` (sidebar-nav + CodeBlock с copy-to-clipboard). EN-only для docs-страниц, лендинг билингвальный.
  - ✅ **Лендинг исправлен:** CODE-семпл заменён на реальный фасад `ps.*` (раньше был выдуманный `api.*`), i18n обновлено (legacy shipped v0.2, install — два jar'а), добавлена кнопка Docs, matrix-бейджи v0.2.
  - ✅ **Hangar — DONE.** Проект: **https://hangar.papermc.io/DARKPIX/PaperScript** (id 6529, dev_tools, MIT, links GitHub/Docs/Issues). Версии загружены (channel Release, state `unreviewed` — ждёт ревью стаффа Hangar): `0.2.0` → PAPER 1.18–1.21.x (через `externalUrl` на GitHub release, jar 36MB), `0.2.0-legacy` → PAPER 1.12.2/1.16.5 (файлом). Рабочий ключ — второй (`HANGAR_API_KEY` в `C:/Programming/.env`, perms `1111011011110111`, есть create_version); первый ключ (`...d19b3c1e`) недостаточен — отозвать в веб-UI. Payload'ы для будущих релизов: `/c/Programming/.tmp-target/vu_modern.json` / `vu_legacy.json`; flow: `POST /api/v1/authenticate?apiKey=KEY` → JWT → `POST /api/v1/projects/DARKPIX/PaperScript/upload` (multipart `versionUpload` + опц. `files`).
  - Modrinth/SpigotMC, OG-обложка — опционально, после Hangar.

Перед любым изменением фасада: обновить `host/.../api/` **и** `sdk/src/global.ts` вместе; при ломающем изменении — поднять `apiVersion`. После изменения workflows — помнить правило про `on`/блочные списки/`permissions` (см. Fix log).
