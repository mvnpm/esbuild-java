package ch.nerdin.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class ImportToPackageTest {

    @Test
    public void testTemplate() {
        // when
        final String convert = ImportToPackage.convert("easymde", "2.16.2", "dist/test.js");

        // then
        assertEquals("""
                {
                  "name": "easymde",
                  "version": "2.16.2",
                  "main": "dist/test.js",
                  "browser": {
                    "fs": false,
                    "path": false,
                    "os": false
                  }
                }""", convert);
    }

    @Test
    public void convert() throws URISyntaxException {
        final File file = new File(getClass().getResource("/import-map.json").toURI());
        final ImportToPackage.PackageInfo stimulus = ImportToPackage.extractInfo(file.toPath());

        assertEquals("./dist/stimulus.js", stimulus.getMain());
        assertEquals("/_static/stimulus/", stimulus.getDirectory());
        assertEquals("stimulus", stimulus.getName());
    }

    @Test
    public void convert2() throws URISyntaxException {
        final File file = new File(getClass().getResource("/import-map-2.json").toURI());
        final ImportToPackage.PackageInfo babelRuntime = ImportToPackage.extractInfo(file.toPath());

        assertEquals("index.js", babelRuntime.getMain());
        assertEquals("/_static/babel-runtime/", babelRuntime.getDirectory());
        assertEquals("@babel/runtime", babelRuntime.getName());
    }
}
