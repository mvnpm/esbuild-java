package ch.nerdin.esbuild.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntryPointTest {

    @Test
    public void test() throws URISyntaxException {
        // given
        final File resource1 = new File(getClass().getResource("/multi/script1.js").toURI());
        final File resource2 = new File(getClass().getResource("/multi/script2.js").toURI());

        // when
        final String convert = EntryPoint.convert(List.of(resource1, resource2));

        // then
        assertEquals("""
import * as script1 from "./script1";
import * as script2 from "./script2";
                """, convert);
    }
}
