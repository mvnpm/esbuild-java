package io.mvnpm.esbuild.script;

public class CommonScript {

    // language=JavaScript
    public static final String COMMON = """
            import * as esbuild from 'esbuild';

            %s

            const color = %s;
            const plugins = %s;

            function cleanLog(log) {
                return log.replace(/(?:\\r\\n|\\r|\\n)/g, '<br>').replace('[ERROR]', '').replace('[WARNING]', '');
            }

            function captureLogsPlugin() {
                return {
                    name: 'capture-logs', setup(build) {
                        build.onEnd(async result => {
                            if (result.errors.length > 0) {
                                let formatted = await esbuild.formatMessages(result.errors, { kind: 'error', color, terminalWidth: 100 });
                                formatted.forEach((f) => console.log('[ERROR] ' + cleanLog(f)));
                            }
                            if (result.warnings.length > 0) {
                                let formatted = await esbuild.formatMessages(result.warnings, { kind: 'warning', color, terminalWidth: 100 });
                                formatted.forEach((f) => console.log('[WARN] ' + cleanLog(f)));
                            }
                        });
                    }
                };
            }

            function applyPlugins(config) {
                let newConfig = config;
                newConfig.logLevel = 'silent';
                if (!newConfig.plugins) {
                    newConfig.plugins = [captureLogsPlugin()];
                }
                for (const plugin of plugins) {
                    console.log(`[DEBUG] Adding ${plugin.name}`);
                    try {
                        const mapper = eval(plugin.buildConfigMapper);
                        newConfig = mapper(config, plugin.data);
                    } catch (err) {
                        console.error(`[FATAL] Error while applying plugin ${plugin.name}`, err);
                        process.exit(1);
                    }

                }
                return newConfig;
            }

            """;

}
