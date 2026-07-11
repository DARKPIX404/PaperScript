import { build, type BuildOptions } from 'esbuild';
import fs from 'node:fs';
import path from 'node:path';

/**
 * Bundle a single TS entry into one self-contained JS file suitable for a
 * PaperScript host (iife, side-effect based registration via the global `ps`).
 *
 * `target` controls the output language level: keep the default (es2022) for
 * the modern GraalJS host; use 'es2015' for the legacy Nashorn line
 * (1.12.2-1.16.5). esbuild's lowest target is es2015, which Nashorn accepts in
 * its ES6 mode (the legacy host enables it).
 */
export async function buildPlugin(
  entry: string,
  outFile: string,
  extra: Partial<BuildOptions> = {},
  target: string = 'es2022'
): Promise<void> {
  fs.mkdirSync(path.dirname(outFile), { recursive: true });
  await build({
    entryPoints: [entry],
    outfile: outFile,
    bundle: true,
    format: 'iife',
    platform: 'neutral',
    target,
    sourcemap: 'linked',
    sourcesContent: true,
    ...extra,
  });
}
