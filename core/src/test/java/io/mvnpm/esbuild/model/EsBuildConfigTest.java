package io.mvnpm.esbuild.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class EsBuildConfigTest {

    @Test
    public void shouldOutputFlags() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.bundle(true);
        esBuildConfig.format(EsBuildConfig.Format.ESM);

        // when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--bundle", "--format=esm"));
    }

    @Test
    public void shouldOutputLoaderFlags() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.loader(Map.of(".js", EsBuildConfig.Loader.JSX, ".css", EsBuildConfig.Loader.LOCAL_CSS));

        // when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--loader:.js=jsx", "--loader:.css=local-css"));
    }

    @Test
    public void shouldOutputPublicPathFlag() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.publicPath("https://www.example.com/v1");

        // when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--public-path=https://www.example.com/v1"));
    }

    @Test
    public void shouldOutputStandardFlags() {
        // given
        final EsBuildConfig esBuildConfig = EsBuildConfig.builder().bundle()
                .entryPoint(new String[] { "main.js", "bundle.js" }).outDir("/tmp").minify().build();

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--bundle", "main.js", "bundle.js", "--minify", "--format=esm",
                "--loader:.svg=file",
                "--loader:.gif=file", "--loader:.css=css", "--loader:.jpg=file", "--loader:.eot=file", "--loader:.json=json",
                "--loader:.ts=ts", "--loader:.png=file", "--loader:.ttf=file", "--loader:.woff2=file", "--loader:.jsx=jsx",
                "--loader:.js=js", "--loader:.woff=file", "--loader:.tsx=tsx", "--outdir=/tmp", "--sourcemap",
                "--splitting", "--entry-names=[name]-[hash]", "--asset-names=assets/[name]-[hash]"));
    }

    @Test
    public void shouldExternal() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.addExternal("*.png");
        esBuildConfig.addExternal("/images/*");

        // when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--external:*.png", "--external:/images/*"));
    }

    @Test
    public void shouldAddChunkNames() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.chunkNames("chunks/[name]-[hash]");

        //when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--chunk-names=chunks/[name]-[hash]"));
    }

    @Test
    public void shouldNotChangeCase() {
        // given
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.define.put("FOO", "BAR");
        esBuildConfig.define.put("foo", "bar");

        // when
        final String[] params = esBuildConfig.build().toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--define:FOO=BAR", "--define:foo=bar"));
    }

}
