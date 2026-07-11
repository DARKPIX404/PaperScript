import { build, context, type BuildOptions } from 'esbuild';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const [, , cmd, ...rest] = process.argv;

function entryAndOut(): { entry: string; out: string } {
  const cwd = process.cwd();
  return {
    entry: path.join(cwd, 'src', 'index.ts'),
    out: path.join(cwd, 'dist', 'index.js'),
  };
}

/**
 * Output language level. Reads `target` from the plugin's plugin.json so one TS
 * source can bundle for both hosts: default es2022 (modern GraalJS), or
 * "es2015" for the legacy Nashorn line (1.12.2-1.16.5).
 */
function readTarget(): string {
  try {
    const manifest = JSON.parse(
      fs.readFileSync(path.join(process.cwd(), 'plugin.json'), 'utf8')
    );
    return typeof manifest.target === 'string' ? manifest.target : 'es2022';
  } catch {
    return 'es2022';
  }
}

function baseOpts(entry: string, outfile: string, target: string): BuildOptions {
  return {
    entryPoints: [entry],
    outfile,
    bundle: true,
    format: 'iife',
    platform: 'neutral',
    target,
    sourcemap: 'linked',
    logLevel: 'info',
  };
}

async function runBuild(): Promise<void> {
  const { entry, out } = entryAndOut();
  const target = readTarget();
  fs.mkdirSync(path.dirname(out), { recursive: true });
  await build(baseOpts(entry, out, target));
  console.log('built ->', path.relative(process.cwd(), out), `(${target})`);
}

async function runDev(): Promise<void> {
  const { entry, out } = entryAndOut();
  fs.mkdirSync(path.dirname(out), { recursive: true });
  const ctx = await context(baseOpts(entry, out, readTarget()));
  await ctx.watch();
  console.log(
    'watching',
    path.relative(process.cwd(), entry),
    '->',
    path.relative(process.cwd(), out),
    '(Ctrl+C to stop)'
  );
}

async function runInit(): Promise<void> {
  const name = rest[0] || 'my-plugin';
  const here = path.dirname(fileURLToPath(import.meta.url));
  const template = path.resolve(here, '..', 'templates', 'hello');
  const dest = path.resolve(process.cwd(), name);

  fs.cpSync(template, dest, { recursive: true });

  const pkgPath = path.join(dest, 'package.json');
  const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
  pkg.name = name;
  fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n');

  const manifestPath = path.join(dest, 'plugin.json');
  const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
  manifest.name = name;
  fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2) + '\n');

  console.log('created', path.relative(process.cwd(), dest));
}

function help(): void {
  console.log('Usage: paperscript <init [name] | build | dev>');
}

try {
  if (cmd === 'build') await runBuild();
  else if (cmd === 'dev') await runDev();
  else if (cmd === 'init') await runInit();
  else help();
} catch (err) {
  console.error(err);
  process.exit(1);
}
