const esbuild = require("esbuild");
const plugin = require("esbuild-sass-plugin");

const { watch, ...config } = (
    <% config %>
);

if (watch) {
    esbuild.context({
        ...config,
        plugins: [plugin.sassPlugin()]
    }).then(ctx => ctx.watch());
} else {
    esbuild.build({
        ...config,
        plugins: [plugin.sassPlugin()]
    }).then(() => console.log("build finished"));
}