package io.mvnpm.esbuild;

import static io.mvnpm.esbuild.BundlerTestHelper.getBundleOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.DevResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.WebDependency.WebDependencyType;
import io.mvnpm.esbuild.script.DevProcess;

public class BundlerDevTestBase {

    @Test
    public void shouldDev() throws URISyntaxException, IOException, InterruptedException {
        // given
        final BundleOptions options = getBundleOptions(List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-mvnpm.js").withEsConfig(EsBuildConfig.builder().fixedEntryNames().build()).build();

        // when
        AtomicReference<BundlingException> bundleException = new AtomicReference<>();
        final DevResult devResult = Bundler.dev(options, true);

        // then
        DevProcess process = devResult.process();
        assertTrue(process.isAlive(), "process is alive");
        try (process) {
            process.build();
            assertTrue(process.isAlive(), "process is alive");
            final Path app = process.workDir().resolve("application-mvnpm.js");
            assertTrue(Files.exists(app));
            final Path distApp = process.dist().resolve("application-mvnpm.js");
            assertTrue(Files.exists(distApp));

            //  when
            Files.writeString(app, "\nalert(\"foo\");", StandardOpenOption.APPEND);
            process.build();
            assertTrue(process.isAlive(), "process is alive");
            assertNull(bundleException.get(), "No error during bundling");
            assertTrue(Files.readString(distApp).contains("alert(\"foo\");"));
        }

        assertFalse(process.isAlive());
    }

    @Test
    public void shouldMultipleDevInMultipleThreads() throws URISyntaxException, IOException, InterruptedException {
        final BundleOptions options = getBundleOptions(
                List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-mvnpm.js").withEsConfig(
                        EsBuildConfig.builder()
                                .fixedEntryNames()
                                .define("foo", "\"bar")
                                .build())
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<DevResult> r = new AtomicReference<>();
        int targetCount = 5;
        for (int i = 0; i < targetCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                System.out.println("Running task " + idx);
                try (final DevResult result = Bundler.dev(options, true)) {
                    result.process().build();
                    r.set(result);
                } catch (BundlingException e) {
                    // good
                    counter.incrementAndGet();
                    System.out.println("good");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        assertEquals(targetCount, counter.get());

        if (r.get() != null) {
            throw new RuntimeException(r.get().toString());
        }
    }

    @Test
    public void shouldDevInMultipleThreads() throws URISyntaxException, IOException, InterruptedException {
        final BundleOptions options = getBundleOptions(
                List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-mvnpm.js").withEsConfig(
                        EsBuildConfig.builder()
                                .fixedEntryNames()
                                .define("foo", "\"bar")
                                .build())
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<DevResult> r = new AtomicReference<>();
        int targetCount = 5;

        try (final DevResult result = Bundler.dev(options, true)) {
            for (int i = 0; i < targetCount; i++) {
                final int idx = i;
                executor.submit(() -> {
                    System.out.println("Running task " + idx);
                    try {
                        result.process().build();
                        r.set(result);
                    } catch (BundlingException e) {
                        // good
                        counter.incrementAndGet();
                        System.out.println("good");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        assertEquals(targetCount, counter.get());

        if (r.get() != null) {
            throw new RuntimeException(r.get().toString());
        }
    }

    @Test
    public void shouldDevWithError() throws URISyntaxException, IOException, InterruptedException {

        // given
        final BundleOptions options = getBundleOptions(List.of("/mvnpm/stimulus-3.2.1.jar"),
                WebDependencyType.MVNPM,
                "application-error.js").withEsConfig(EsBuildConfig.builder().fixedEntryNames().build()).build();

        // when
        final DevResult devResult = Bundler.dev(options, true);

        // then
        DevProcess process = devResult.process();
        assertTrue(process.isAlive(), "process is alive");
        try (process) {

            assertThrows(BundlingException.class, process::build);
            assertTrue(process.isAlive(), "process is alive");

            final Path app = process.workDir().resolve("application-error.js");
            assertTrue(Files.exists(app));

            // when
            Files.writeString(app, "alert(\"foo\");", StandardOpenOption.TRUNCATE_EXISTING);

            // then
            process.build();
            final Path distApp = process.dist().resolve("application-error.js");
            assertTrue(Files.exists(distApp));
            assertTrue(Files.readString(distApp).contains("alert(\"foo\");"));

        }
        // then

    }

}
