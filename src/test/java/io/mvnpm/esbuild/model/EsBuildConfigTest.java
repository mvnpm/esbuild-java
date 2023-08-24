package io.mvnpm.esbuild.model;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EsBuildConfigTest {

    @Test
    public void shouldOutputFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setBundle(true);
        esBuildConfig.setFormat(EsBuildConfig.Format.ESM);

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--bundle", "--format=esm"));
    }

    @Test
    public void shouldOutputLoaderFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setLoader(Map.of(".js", EsBuildConfig.Loader.JSX, ".css", EsBuildConfig.Loader.LOCAL_CSS));

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--loader:.js=jsx", "--loader:.css=local-css"));
    }

    @Test
    public void shouldOutputStandardFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfigBuilder().bundle()
                .entryPoint(new String[]{"main.js", "bundle.js"}).outDir("/tmp").minify().build();

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--bundle", "main.js", "bundle.js", "--minify", "--format=esm", "--loader:.svg=file",
            "--loader:.gif=file", "--loader:.css=css", "--loader:.jpg=file", "--loader:.eot=file", "--loader:.json=json",
            "--loader:.ts=ts", "--loader:.png=file", "--loader:.ttf=file", "--loader:.woff2=file", "--loader:.jsx=jsx",
            "--loader:.js=js", "--loader:.woff=file", "--loader:.tsx=tsx", "--outdir=/tmp", "--sourcemap",
            "--splitting", "--entry-names=[name]-[hash]", "--asset-names=assets/[name]-[hash]"));
    }

    @Test
    public void shouldExternal() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.addExternal("*.png");
        esBuildConfig.addExternal("/images/*");

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--external:*.png", "--external:/images/*"));
    }

    @Test
    public void shouldAddChunkNames() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setChunkNames("chunks/[name]-[hash]");

        //when
        final String[] params = esBuildConfig.toParams();

        // then
        assertThat(asList(params), containsInAnyOrder("--chunk-names=chunks/[name]-[hash]"));
    }
}
