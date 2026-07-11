# host-legacy (PaperScript for 1.12.2–1.16.5)

Status: **foundation stub** (v1). The JS engine lands in **v1.1** — see `docs/legacy.md`.

- Target: Bukkit/Spigot 1.12.2–1.16.5 on **Java 8/11**.
- Engine (planned): **Nashorn** (`javax.script`, bundled in Java 8/11) — GraalJS needs Java 17+ and cannot run here.
- Chat (planned): legacy `§` color codes via `ChatColor.translateAlternateColorCodes('&', …)` (no Adventure/MiniMessage on these versions).
- Facade: will mirror the modern `ps.*` API so the same plugin scripts run on both lines.
