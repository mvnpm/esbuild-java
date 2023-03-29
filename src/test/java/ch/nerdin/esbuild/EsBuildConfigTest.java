package ch.nerdin.esbuild;

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
        esBuildConfig.setLoader(Map.of(".js", EsBuildConfig.Loader.JSX));

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertArrayEquals(new String[]{"--loader:.js=jsx"}, params);
    }

    @Test
    public void shouldOutputStandardFlags() {
        // given
        final EsBuildConfig esBuildConfig = new EsBuildConfigBuilder().bundle().entryPoint("main.js").outDir("/tmp").minify().build();

        // when
        final String[] params = esBuildConfig.toParams();

        // then
        assertArrayEquals(new String[]{"--bundle", "main.js", "--minify", "--outdir=/tmp"}, params);
    }
}
