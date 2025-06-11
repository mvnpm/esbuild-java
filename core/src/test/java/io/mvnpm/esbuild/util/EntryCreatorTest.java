package io.mvnpm.esbuild.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.EsBuildConfig;

public class EntryCreatorTest {

    @Test
    void createEntryJs() throws URISyntaxException, IOException {
        // given
        Path test = Files.createTempDirectory("createEntryJsTest");
        EsBuildConfig esBuildConfig = EsBuildConfig.builder().entryPoint(new String[] { "app.js" })
                .outDir(test.resolve("dist").toString()).format(EsBuildConfig.Format.ESM).build();

        // when
        EntryCreator.createEntryJs(test, esBuildConfig);

        // then
        Path output = test.resolve("index.js");
        assertTrue(Files.exists(output));
    }
}
