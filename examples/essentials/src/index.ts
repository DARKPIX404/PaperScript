// Essentials: стандартные команды (spawn/tp/tpa/heal/feed/fly/gamemode/home)
// с единым оформлением чата через MiniMessage.

const PREFIX = "<dark_gray>[<gradient:#ff7a18:#af52de>Essentials</gradient>]</dark_gray> ";

const ok = (t: string) => `<green>${t}</green>`;
const fail = (t: string) => `<red>${t}</red>`;
const info = (t: string) => `<gray>${t}</gray>`;
const hi = (t: string) => `<gold>${t}</gold>`;
const aqua = (t: string) => `<aqua>${t}</aqua>`;

interface MessageTarget {
  sendMessage(msg: string): void;
}

function tell(target: MessageTarget, text: string): void {
  target.sendMessage(PREFIX + text);
}

function self(ctx: CommandContext): Player | null {
  if (!ctx.sender.player) {
    tell(ctx.sender, fail("Эта команда только для игроков."));
    return null;
  }
  const p = ps.players.get(ctx.sender.name);
  if (!p) tell(ctx.sender, fail("Не удалось найти игрока."));
  return p;
}

function findPlayer(name: string | null, ctx: CommandContext): Player | null {
  if (!name) {
    tell(ctx.sender, info("Укажите ник игрока."));
    return null;
  }
  const p = ps.players.get(name);
  if (!p) tell(ctx.sender, fail(`Игрок ${hi(name)} не в сети.`));
  return p;
}

function needOp(ctx: CommandContext): boolean {
  if (!ctx.sender.op) {
    tell(ctx.sender, fail("Недостаточно прав."));
    return false;
  }
  return true;
}

function worldOf(p: Player): World | null {
  return ps.worlds.get(p.location.world) ?? ps.worlds.all()[0] ?? null;
}

// ---- /spawn -------------------------------------------------------------
ps.commands.register(
  "spawn",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const w = worldOf(p);
    if (!w) {
      tell(ctx.sender, fail("Мир не найден."));
      return;
    }
    p.teleport(w.spawnLocation);
    tell(ctx.sender, ok("Вы телепортированы на спавн."));
  },
  "Телепорт на спавн",
  "/spawn"
);

// ---- /setspawn ----------------------------------------------------------
ps.commands.register(
  "setspawn",
  (ctx) => {
    if (!needOp(ctx)) return;
    const p = self(ctx);
    if (!p) return;
    const w = worldOf(p);
    if (!w) {
      tell(ctx.sender, fail("Мир не найден."));
      return;
    }
    w.setSpawnLocation(p.location);
    tell(ctx.sender, ok(`Точка спавна установлена в мире ${aqua(w.name)}.`));
  },
  "Установить точку спавна",
  "/setspawn"
);

// ---- /tp ----------------------------------------------------------------
ps.commands.register(
  "tp",
  (ctx) => {
    const a = ctx.arg(0);
    const b = ctx.arg(1);
    if (a && b) {
      if (!needOp(ctx)) return;
      const from = findPlayer(a, ctx);
      const to = findPlayer(b, ctx);
      if (!from || !to) return;
      from.teleport(to.location);
      tell(ctx.sender, ok(`${hi(from.name)} телепортирован к ${hi(to.name)}.`));
    } else if (a) {
      const p = self(ctx);
      if (!p) return;
      const to = findPlayer(a, ctx);
      if (!to) return;
      p.teleport(to.location);
      tell(ctx.sender, ok(`Телепорт к ${hi(to.name)}.`));
    } else {
      tell(ctx.sender, info("Использование: /tp <игрок> или /tp <a> <b>"));
    }
  },
  "Телепортация",
  "/tp <игрок>"
);

// ---- /tpa + /tpaccept ---------------------------------------------------
interface TpaRequest {
  from: string;
  task: number;
}
const pending = new Map<string, TpaRequest>(); // key = имя цели

ps.commands.register(
  "tpa",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const t = findPlayer(ctx.arg(0), ctx);
    if (!t) return;
    if (t.name === p.name) {
      tell(ctx.sender, fail("Нельзя отправить запрос себе."));
      return;
    }
    const prev = pending.get(t.name);
    if (prev) ps.scheduler.cancelTask(prev.task);
    const task = ps.scheduler.runTaskLater(() => pending.delete(t.name), 20 * 60);
    pending.set(t.name, { from: p.name, task });
    tell(ctx.sender, ok(`Запрос на ТП отправлен ${hi(t.name)}.`));
    t.sendMessage(
      PREFIX +
        info(`${hi(p.name)} просит телепортироваться к вам. `) +
        aqua(`/tpaccept ${p.name}`) +
        info(" (60с).")
    );
  },
  "Запрос на телепорт",
  "/tpa <игрок>"
);

ps.commands.register(
  "tpaccept",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const req = pending.get(p.name);
    if (!req) {
      tell(ctx.sender, fail("Нет входящих запросов."));
      return;
    }
    const who = ctx.arg(0);
    if (who && who.toLowerCase() !== req.from.toLowerCase()) {
      tell(ctx.sender, fail(`Запрос от ${hi(who)} не найден.`));
      return;
    }
    const from = ps.players.get(req.from);
    pending.delete(p.name);
    ps.scheduler.cancelTask(req.task);
    if (!from) {
      tell(ctx.sender, fail("Игрок уже не в сети."));
      return;
    }
    from.teleport(p.location);
    from.sendMessage(PREFIX + ok(`${hi(p.name)} принял запрос. Телепортация...`));
    tell(ctx.sender, ok(`Вы приняли запрос от ${hi(from.name)}.`));
  },
  "Принять запрос на ТП",
  "/tpaccept [игрок]"
);

// ---- /heal --------------------------------------------------------------
ps.commands.register(
  "heal",
  (ctx) => {
    const name = ctx.arg(0);
    if (name) {
      if (!needOp(ctx)) return;
      const t = findPlayer(name, ctx);
      if (!t) return;
      t.heal();
      t.sendMessage(PREFIX + ok("Вас исцелили."));
      tell(ctx.sender, ok(`${hi(t.name)} исцелён.`));
    } else {
      const p = self(ctx);
      if (!p) return;
      p.heal();
      tell(ctx.sender, ok("Вы исцелены."));
    }
  },
  "Исцелить",
  "/heal [игрок]"
);

// ---- /feed --------------------------------------------------------------
ps.commands.register(
  "feed",
  (ctx) => {
    const name = ctx.arg(0);
    if (name) {
      if (!needOp(ctx)) return;
      const t = findPlayer(name, ctx);
      if (!t) return;
      t.feed();
      t.sendMessage(PREFIX + ok("Ваш голод утолён."));
      tell(ctx.sender, ok(`${hi(t.name)} накормлен.`));
    } else {
      const p = self(ctx);
      if (!p) return;
      p.feed();
      tell(ctx.sender, ok("Вы накормлены."));
    }
  },
  "Утолить голод",
  "/feed [игрок]"
);

// ---- /fly ---------------------------------------------------------------
ps.commands.register(
  "fly",
  (ctx) => {
    const name = ctx.arg(0);
    const toggle = (p: Player): boolean => {
      const next = !p.allowFlight;
      p.allowFlight = next;
      if (!next) p.flying = false;
      return next;
    };
    if (name) {
      if (!needOp(ctx)) return;
      const t = findPlayer(name, ctx);
      if (!t) return;
      const next = toggle(t);
      t.sendMessage(PREFIX + info(`Полёт ${next ? ok("включён") : fail("выключен")}.`));
      tell(ctx.sender, ok(`Полёт для ${hi(t.name)} ${next ? "включён" : "выключен"}.`));
    } else {
      const p = self(ctx);
      if (!p) return;
      const next = toggle(p);
      tell(ctx.sender, info(`Полёт ${next ? ok("включён") : fail("выключен")}.`));
    }
  },
  "Режим полёта",
  "/fly [игрок]"
);

// ---- /gamemode ----------------------------------------------------------
ps.commands.register(
  "gamemode",
  (ctx) => {
    if (!needOp(ctx)) return;
    const mode = ctx.arg(0);
    if (!mode) {
      tell(ctx.sender, info("Использование: /gamemode <survival|creative|adventure|spectator> [игрок]"));
      return;
    }
    const apply = (p: Player): boolean => {
      if (!p.setGameMode(mode)) {
        tell(ctx.sender, fail(`Неизвестный режим: ${hi(mode)}.`));
        return false;
      }
      return true;
    };
    const name = ctx.arg(1);
    if (name) {
      const t = findPlayer(name, ctx);
      if (!t || !apply(t)) return;
      t.sendMessage(PREFIX + info(`Ваш режим игры: ${aqua(t.gameMode)}.`));
      tell(ctx.sender, ok(`Режим ${hi(t.name)}: ${aqua(t.gameMode)}.`));
    } else {
      const p = self(ctx);
      if (!p || !apply(p)) return;
      tell(ctx.sender, info(`Ваш режим игры: ${aqua(p.gameMode)}.`));
    }
  },
  "Сменить режим игры",
  "/gamemode <режим> [игрок]"
);

// ---- /home --------------------------------------------------------------
type HomeMap = Record<string, Location>;

function loadHomes(): HomeMap {
  const raw = ps.storage.get("homes");
  if (!raw) return {};
  try {
    return JSON.parse(raw) as HomeMap;
  } catch {
    return {};
  }
}

function saveHomes(map: HomeMap): void {
  ps.storage.set("homes", JSON.stringify(map));
}

ps.commands.register(
  "sethome",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const name = ctx.arg(0) ?? "home";
    const homes = loadHomes();
    homes[name] = p.location;
    saveHomes(homes);
    tell(ctx.sender, ok(`Точка дома ${aqua(name)} сохранена.`));
  },
  "Сохранить точку дома",
  "/sethome [имя]"
);

ps.commands.register(
  "home",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const name = ctx.arg(0) ?? "home";
    const homes = loadHomes();
    const loc = homes[name];
    if (!loc) {
      tell(ctx.sender, fail(`Точка дома ${aqua(name)} не найдена.`));
      return;
    }
    p.teleport(loc);
    tell(ctx.sender, ok(`Вы дома (${aqua(name)}).`));
  },
  "Телепорт домой",
  "/home [имя]"
);

ps.commands.register(
  "delhome",
  (ctx) => {
    const p = self(ctx);
    if (!p) return;
    const name = ctx.arg(0) ?? "home";
    const homes = loadHomes();
    if (!homes[name]) {
      tell(ctx.sender, fail(`Точка дома ${aqua(name)} не найдена.`));
      return;
    }
    delete homes[name];
    saveHomes(homes);
    tell(ctx.sender, ok(`Точка дома ${aqua(name)} удалена.`));
  },
  "Удалить точку дома",
  "/delhome [имя]"
);

ps.onEnable(() => {
  ps.logger.info("Essentials loaded: spawn/tp/tpa/heal/feed/fly/gamemode/home");
});

ps.onDisable(() => {
  for (const req of pending.values()) ps.scheduler.cancelTask(req.task);
  pending.clear();
  ps.logger.info("Essentials disabled.");
});
