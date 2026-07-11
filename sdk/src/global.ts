// Typed facade that mirrors the Java `dev.paperscript.host.api` package.
// Keep this in sync with the Java side — it is the source of truth for
// plugin-author intellisense, while the Java facade is the runtime truth.
//
// Note: every `sendMessage`/`broadcast` accepts a MiniMessage string
// (`<red>`, `<gradient:#a:#b>`, `<bold>`, hover/click, ...).

export interface PaperScript {
  readonly logger: Logger;
  readonly events: Events;
  readonly commands: Commands;
  readonly scheduler: Scheduler;
  readonly players: Players;
  readonly worlds: Worlds;
  readonly server: Server;
  readonly loc: Locations;
  readonly storage: Storage;

  onEnable(fn: () => void): void;
  onDisable(fn: () => void): void;
}

export interface Logger {
  info(message: string): void;
  warn(message: string): void;
  error(message: string): void;
}

export interface Location {
  readonly world: string;
  readonly x: number;
  readonly y: number;
  readonly z: number;
  readonly yaw: number;
  readonly pitch: number;
}

export interface Player {
  readonly name: string;
  readonly uniqueId: string;
  readonly online: boolean;

  health: number;
  foodLevel: number;

  readonly location: Location;
  teleport(loc: Location): boolean;

  heal(): void;
  feed(): void;

  allowFlight: boolean;
  flying: boolean;

  readonly gameMode: string;
  setGameMode(mode: "survival" | "creative" | "adventure" | "spectator" | string): boolean;

  sendMessage(message: string): void;
}

export interface JoinEvent {
  readonly player: Player;
}

export interface QuitEvent {
  readonly player: Player;
}

export interface Events {
  onPlayerJoin(handler: (event: JoinEvent) => void): void;
  onPlayerQuit(handler: (event: QuitEvent) => void): void;
}

export interface Sender {
  readonly name: string;
  readonly op: boolean;
  readonly player: boolean;
  sendMessage(message: string): void;
}

export interface CommandContext {
  readonly sender: Sender;
  readonly label: string;
  readonly args: string[];
  arg(index: number): string | null;
}

export interface Commands {
  register(
    name: string,
    handler: (ctx: CommandContext) => void,
    description?: string,
    usage?: string
  ): void;
}

export interface Scheduler {
  runTask(fn: () => void): number;
  runTaskLater(fn: () => void, delayTicks: number): number;
  runTaskTimer(fn: () => void, delayTicks: number, periodTicks: number): number;
  cancelTask(taskId: number): void;
}

export interface Players {
  online(): Player[];
  get(name: string): Player | null;
}

export interface World {
  readonly name: string;
  time: number;
  readonly playerCount: number;
  readonly spawnLocation: Location;
  setSpawnLocation(loc: Location): void;
}

export interface Worlds {
  all(): World[];
  get(name: string): World | null;
}

export interface Locations {
  create(world: string, x: number, y: number, z: number): Location;
  create(world: string, x: number, y: number, z: number, yaw: number, pitch: number): Location;
}

export interface Storage {
  /** Returns the stored JSON string, or null. Use `JSON.parse`. */
  get(key: string): string | null;
  /** Stores a value as a JSON string. Use `JSON.stringify`. */
  set(key: string, jsonValue: string): void;
  remove(key: string): void;
  save(): void;
}

export interface Server {
  readonly version: string;
  readonly minecraftVersion: string;
  readonly onlineCount: number;
  readonly maxPlayers: number;
  broadcast(message: string): void;
}

declare global {
  // Bound by the host into every script context.
  const ps: PaperScript;
}

export {};
