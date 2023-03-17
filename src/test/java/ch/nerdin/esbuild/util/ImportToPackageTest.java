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
                  "main": "dist/test.js"
                }""", convert);
    }

    @Test
    public void convert() throws URISyntaxException {
        final File file = new File(getClass().getResource("/import-map.json").toURI());
        final String stimulus = ImportToPackage.extractInfo(file.toPath(), "stimulus");

        assertEquals("/_static/stimulus/./dist/stimulus.js", stimulus);
    }
}
