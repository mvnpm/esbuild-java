package io.mvnpm.esbuild.script;

public class CommonScript {

    // language=JavaScript
    public static final String COMMON = """
            import * as esbuild from 'esbuild';
            %s

            const plugins = %s;

            function applyPlugins(config) {
                let newConfig = config;
                if (!newConfig.plugins) {
                    newConfig.plugins = [];
                }
                for (const plugin of plugins) {
                    console.debug(`Adding ${plugin.name}`);
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
