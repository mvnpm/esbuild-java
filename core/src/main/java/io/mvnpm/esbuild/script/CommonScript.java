package io.mvnpm.esbuild.script;

public class CommonScript {

    // language=JavaScript
    public static final String COMMON = """
            import * as esbuild from 'esbuild';
            %s

            const plugins = %s;

            function resolvePlugins() {
                const requiredPlugins = [];
                console.log(plugins);
                for (const plugin of plugins) {
                    console.debug(`Adding ${plugin.name}`);
                    const data = plugin.data;
                    requiredPlugins.push(eval(plugin.requireScript));
                }
                return requiredPlugins;
            }

            """;

}
