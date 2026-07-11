import { build, type BuildOptions } from 'esbuild';
import fs from 'node:fs';
import path from 'node:path';

/**
 * Bundle a single TS entry into one self-contained JS file suitable for the
 * GraalJS host (iife, side-effect based registration via the global `ps`).
 */
export async function buildPlugin(
  entry: string,
  outFile: string,
  extra: Partial<BuildOptions> = {}
): Promise<void> {
  fs.mkdirSync(path.dirname(outFile), { recursive: true });
  await build({
    entryPoints: [entry],
    outfile: outFile,
    bundle: true,
    format: 'iife',
    platform: 'neutral',
    target: 'es2022',
    sourcemap: 'linked',
    sourcesContent: true,
    ...extra,
  });
}
