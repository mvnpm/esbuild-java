const esbuild = require("esbuild");
const plugin = require("esbuild-sass-plugin");

const { version, watch, ...config } = (
    <% config %>
);

if (version) {
    console.log("${esbuild.version}");
}

if (watch) {
    esbuild.context({
        ...config,
        plugins: [
            plugin.sassPlugin(),
            {
                name: 'build-notifier',
                setup(build) {
                    build.onEnd((result) => {
                        if (result.errors.length > 0) {
                            buildEventHandler.onBuildError("Build failed", result.errors[0].text);
                        } else {
                            buildEventHandler.onBuildComplete("Build completed");
                        }
                    });
                }
            }
        ]
    }).then(ctx => ctx.watch());
} else {
    esbuild.build({
        ...config,
        plugins: [plugin.sassPlugin()]
    }).then(() => console.log("build finished"));
}