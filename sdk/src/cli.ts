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

function baseOpts(entry: string, outfile: string): BuildOptions {
  return {
    entryPoints: [entry],
    outfile,
    bundle: true,
    format: 'iife',
    platform: 'neutral',
    target: 'es2022',
    logLevel: 'info',
  };
}

async function runBuild(): Promise<void> {
  const { entry, out } = entryAndOut();
  fs.mkdirSync(path.dirname(out), { recursive: true });
  await build(baseOpts(entry, out));
  console.log('built ->', path.relative(process.cwd(), out));
}

async function runDev(): Promise<void> {
  const { entry, out } = entryAndOut();
  fs.mkdirSync(path.dirname(out), { recursive: true });
  const ctx = await context(baseOpts(entry, out));
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
