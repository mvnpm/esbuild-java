package ch.nerdin.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

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
    public void extractInfoFromBasicPackage() throws URISyntaxException {
        final File file = new File(getClass().getResource("/import-map.json").toURI());
        final List<ImportToPackage.PackageInfo> stimulus = ImportToPackage.extractPackages(file.toPath());
        assertEquals(1, stimulus.size());
        assertEquals("./dist/stimulus.js", stimulus.get(0).getMain());
        assertEquals("/_static/stimulus/", stimulus.get(0).getDirectory());
        assertEquals("stimulus", stimulus.get(0).getName());
    }

    @Test
    public void extractInfoFromBabelRuntime() throws URISyntaxException {
        final File file = new File(getClass().getResource("/babel-runtime/importmap.json").toURI());
        final List<ImportToPackage.PackageInfo> babelRuntime = ImportToPackage.extractPackages(file.toPath());
        assertEquals(1, babelRuntime.size());
        assertEquals("index.js", babelRuntime.get(0).getMain());
        assertEquals("/_static/babel-runtime/", babelRuntime.get(0).getDirectory());
        assertEquals("@babel/runtime", babelRuntime.get(0).getName());
    }

    @Test
    public void extractInfoFromReactBootstrap() throws URISyntaxException {
        final File file = new File(getClass().getResource("/react-bootstrap/importmap.json").toURI());
        final List<ImportToPackage.PackageInfo> babelRuntime = ImportToPackage.extractPackages(file.toPath());
        assertEquals(1, babelRuntime.size());
        assertEquals("index.js", babelRuntime.get(0).getMain());
        assertEquals("/_static/react-bootstrap/esm/", babelRuntime.get(0).getDirectory());
        assertEquals("react-bootstrap", babelRuntime.get(0).getName());
    }
}
