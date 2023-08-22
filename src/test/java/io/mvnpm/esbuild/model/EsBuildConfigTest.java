package io.mvnpm.esbuild.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

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
        assertArrayEquals(new String[]{"--bundle", "--format=esm"}, params);
    }

    @Test
    public void shouldOutputLoaderFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setLoader(Map.of(".js", EsBuildConfig.Loader.JSX, ".css", EsBuildConfig.Loader.LOCAL_CSS));

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertArrayEquals(new String[]{"--loader:.js=jsx", "--loader:.css=local-css"}, params);
    }

    @Test
    public void shouldOutputStandardFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfigBuilder().bundle()
                .entryPoint(new String[]{"main.js", "bundle.js"}).outDir("/tmp").minify().build();

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertArrayEquals(new String[]{"--bundle", "main.js", "bundle.js", "--minify", "--format=esm", "--loader:.svg=file",
                "--loader:.gif=file", "--loader:.css=css", "--loader:.jpg=file", "--loader:.eot=file", "--loader:.json=json",
                "--loader:.ts=ts", "--loader:.png=file", "--loader:.ttf=file", "--loader:.woff2=file", "--loader:.jsx=jsx",
                "--loader:.js=js", "--loader:.woff=file", "--loader:.tsx=tsx", "--outdir=/tmp", "--sourcemap",
                "--splitting", "--entry-names=[name]-[hash]"}, params);
    }

    @Test
    public void shouldAddChunkNames() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfig();
        esBuildConfig.setChunkNames("chunks/[name]-[hash]");

        //when
        final String[] params = esBuildConfig.toParams();

        // then
        assertArrayEquals(new String[]{"--chunk-names=chunks/[name]-[hash]"}, params);
    }
}
