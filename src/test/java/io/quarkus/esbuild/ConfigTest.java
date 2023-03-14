package io.quarkus.esbuild;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ConfigTest {

    @Test
    public void shouldOutputFlags() {
        // given
        final Config config = new Config();
        config.setBundle(true);
        config.setFormat(Config.Format.ESM);

        // when
        final String[] params = config.toParams();

        // then
        assertArrayEquals(new String[]{"--bundle", "--format=esm"}, params);
    }

    @Test
    public void shouldOutputLoaderFlags() {
        // given
        final Config config = new Config();
        config.setLoader(Map.of(".js", Config.Loader.JSX));

        // when
        final String[] params = config.toParams();

        // then
        assertArrayEquals(new String[]{"--loader:.js=jsx"}, params);
    }

    @Test
    public void shouldOutputStandardFlags() {
        // given
        final Config config = new ConfigBuilder().bundle(true).entryPoint("application-mvnpm.js").outDir("/tmp").minify(true).build();

        // when
        final String[] params = config.toParams();

        // then
        assertArrayEquals(new String[]{"--bundle", "application-mvnpm.js", "--minify", "--outdir=/tmp"}, params);
    }
}
