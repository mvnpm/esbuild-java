import {
    compile,
    env,
    Features,
    Instrumentation,
    optimize,
    toSourceMap,
} from '@tailwindcss/node'
import { clearRequireCache } from '@tailwindcss/node/require-cache'
import { Scanner } from '@tailwindcss/oxide'
import fs from 'node:fs/promises'
import path from 'node:path'

const DEBUG = env.DEBUG

/**
 * @param {object} [opts]
 * @param {boolean|{minify?: boolean}} [opts.optimize] Whether to optimize and minify the CSS.
 * @returns {import('esbuild').Plugin}
 */
export default function esbuildPluginTailwind(opts = {}) {
    const shouldOptimize = opts.optimize !== false
    const minify =
        typeof opts.optimize === 'object' ? opts.optimize.minify !== false : true

    let sources = opts.sources || [];
    let base = opts.base || {};
    const roots = new DefaultMap(
        (id) => new Root(id, base.base, base.pattern, sources, /* enableSourceMaps */ false)
    )

    return {
        name: 'tailwindcss',
        setup(build) {
            build.onLoad({ filter: /\.css$/ }, async (args) => {
                if (!isCssFile(args.path)) return

                using I = new Instrumentation()
                DEBUG && I.start('[tailwindcss] Compile CSS')

                const source = await fs.readFile(args.path, 'utf8')
                const root = roots.get(args.path)
                const result = await root.generate(source, I)

                if (!result) {
                    roots.delete(args.path)
                    return { contents: source, loader: 'css' }
                }

                let { code, map } = result

                if (shouldOptimize) {
                    DEBUG && I.start('[tailwindcss] Optimize CSS')
                    const optimized = optimize(code, { minify, map })
                    code = optimized.code
                    DEBUG && I.end('[tailwindcss] Optimize CSS')
                }

                DEBUG && I.end('[tailwindcss] Compile CSS')

                return {
                    contents: code,
                    loader: 'css',
                    resolveDir: path.dirname(args.path),
                }
            })
        },
    }
}

// ────────────────────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────────────────────
function isCssFile(id) {
    const ext = path.extname(id).slice(1)
    return ext === 'css'
}

function idToPath(id) {
    return path.resolve(id.replace(/\?.*$/, ''))
}

class DefaultMap extends Map {
    constructor(factory) {
        super()
        this.factory = factory
    }
    get(key) {
        let value = super.get(key)
        if (value === undefined) {
            value = this.factory(key, this)
            this.set(key, value)
        }
        return value
    }
}

// ────────────────────────────────────────────────────────────────
// Tailwind Root
// ────────────────────────────────────────────────────────────────
class Root {
    compiler
    scanner
    candidates = new Set()
    buildDependencies = new Map()

    constructor(id, base, pattern, sources, enableSourceMaps) {
        this.id = id
        this.sources = sources
        this.base = base || process.cwd()
        this.pattern = pattern || "**/*"
        this.enableSourceMaps = enableSourceMaps
    }

    async generate(content, I) {
        const inputPath = idToPath(this.id)
        const requiresBuild = await this.requiresBuild()
        const inputBase = path.dirname(inputPath)

        if (!this.compiler || !this.scanner || requiresBuild) {
            clearRequireCache([...this.buildDependencies.keys()])
            this.buildDependencies.clear()
            await this.addBuildDependency(inputPath)

            DEBUG && I.start('Setup compiler')
            this.compiler = await compile(content, {
                from: this.enableSourceMaps ? this.id : undefined,
                base: inputBase,
                shouldRewriteUrls: true,
                onDependency: (dep) => this.addBuildDependency(dep),
            })
            DEBUG && I.end('Setup compiler')

            DEBUG && I.start('Setup scanner')
            const baseSource = { base: this.base, pattern: this.pattern, negated: false };
            const sources = (() => {
                if (this.compiler.root === 'none') return []
                if (this.compiler.root === null)
                    return [baseSource]
                return [{ ...this.compiler.root, negated: false }, baseSource]
            })()
                .concat(this.sources)
                .concat(this.compiler.sources.map(s => ({ ...s, base: this.base })))
            this.scanner = new Scanner({ sources })
            DEBUG && I.end('Setup scanner')
        }

        if (
            !(
                this.compiler.features &
                (Features.AtApply |
                    Features.JsPluginCompat |
                    Features.ThemeFunction |
                    Features.Utilities)
            )
        ) {
            return false
        }

        if (this.compiler.features & Features.Utilities) {
            DEBUG && I.start('Scan for candidates')
            for (const c of this.scanner.scan()) this.candidates.add(c)
            DEBUG && I.end('Scan for candidates')
        }

        DEBUG && I.start('Build CSS')
        const code = this.compiler.build([...this.candidates])
        DEBUG && I.end('Build CSS')

        const map = this.enableSourceMaps
            ? toSourceMap(this.compiler.buildSourceMap()).raw
            : undefined

        return { code, map }
    }

    async addBuildDependency(p) {
        let mtime = null
        try {
            mtime = (await fs.stat(p)).mtimeMs
        } catch {}
        this.buildDependencies.set(p, mtime)
    }

    async requiresBuild() {
        for (const [p, mtime] of this.buildDependencies) {
            if (mtime === null) return true
            try {
                const stat = await fs.stat(p)
                if (stat.mtimeMs > mtime) return true
            } catch {
                return true
            }
        }
        return false
    }
}