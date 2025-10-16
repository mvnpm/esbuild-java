package io.mvnpm.esbuild.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EsBuildConfigToJsonTest {

    @Test
    public void shouldOutputFlagsAsJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.bundle(true);
        esBuildConfig.format(EsBuildConfig.Format.ESM);

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        assertTrue(node.get("bundle").asBoolean());
        assertEquals("esm", node.get("format").asText());
    }

    @Test
    public void shouldOutputLoaderFlagsAsJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.loader(Map.of(".js", EsBuildConfig.Loader.JSX, ".css", EsBuildConfig.Loader.LOCAL_CSS));

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        JsonNode loaderNode = node.get("loader");
        assertEquals("jsx", loaderNode.get(".js").asText());
        assertEquals("local-css", loaderNode.get(".css").asText());
    }

    @Test
    public void shouldOutputPublicPathFlagAsJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.publicPath("https://www.example.com/v1");

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        assertEquals("https://www.example.com/v1", node.get("publicPath").asText());
    }

    @Test
    public void shouldOutputStandardFlagsAsJson() throws Exception {
        final EsBuildConfig esBuildConfig = EsBuildConfig.builder().bundle()
                .entryPoint(new String[] { "main.js", "bundle.js" }).outDir("/tmp").minify().build();

        final String json = esBuildConfig.toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        assertTrue(node.get("bundle").asBoolean());
        assertTrue(node.get("minify").asBoolean());
        assertEquals("/tmp", node.get("outdir").asText());

        JsonNode entryPoints = node.get("entryPoints");
        assertTrue(entryPoints.isArray());
        assertTrue(StreamSupport.stream(entryPoints.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toSet())
                .containsAll(List.of("main.js", "bundle.js")));
    }

    @Test
    public void shouldExternalAsJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.addExternal("*.png");
        esBuildConfig.addExternal("/images/*");

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        JsonNode externalNode = node.get("external");
        assertTrue(externalNode.isArray());
        assertTrue(StreamSupport.stream(externalNode.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toSet())
                .containsAll(List.of("*.png", "/images/*")));
    }

    @Test
    public void shouldAddChunkNamesAsJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.chunkNames("chunks/[name]-[hash]");

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        assertEquals("chunks/[name]-[hash]", node.get("chunkNames").asText());
    }

    @Test
    public void shouldNotChangeCaseInDefineJson() throws Exception {
        final EsBuildConfigBuilder esBuildConfig = new EsBuildConfigBuilder();
        esBuildConfig.define.put("FOO", "BAR");
        esBuildConfig.define.put("foo", "bar");

        final String json = esBuildConfig.build().toJson();

        JsonNode node = new ObjectMapper().readTree(json);
        JsonNode defineNode = node.get("define");
        assertEquals("bar", defineNode.get("foo").asText());
        assertEquals("BAR", defineNode.get("FOO").asText()); // values are lowercased
    }
}
