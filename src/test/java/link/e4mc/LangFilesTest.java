package link.e4mc;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

public class LangFilesTest {

    private static final File LANG_DIR = new File("src/main/resources/assets/e4mc_minecraft/lang");

    @Test
    public void allLangFilesContainAllKeys() throws Exception {
        File en = new File(LANG_DIR, "en_US.lang");
        assertTrue("en_US.lang must exist", en.isFile());
        Map<String, String> base = load(en);
        assertFalse("en_US must not be empty", base.isEmpty());

        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);
        for (File f : files) {
            Map<String, String> map = load(f);
            final String IN = " in ";
            for (String k : base.keySet()) {
                assertTrue("Missing key '" + k + "'" + IN + f.getName(), map.containsKey(k));
                String v = map.get(k);
                assertNotNull("Null value for key '" + k + "'" + IN + f.getName(), v);
                assertFalse("Empty value for key '" + k + "'" + IN + f.getName(), v.trim().isEmpty());
            }
        }
    }

    @Test
    public void placeholdersMatchBaseCounts() throws Exception {
        File en = new File(LANG_DIR, "en_US.lang");
        assertTrue("en_US.lang must exist", en.isFile());
        Map<String, String> base = load(en);

        // Pre-compute base placeholder counts per key
        Map<String, Integer> baseCounts = new HashMap<>();
        for (Map.Entry<String, String> e : base.entrySet()) {
            baseCounts.put(e.getKey(), countPlaceholders(e.getValue()));
        }

        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);
        for (File f : files) {
            Map<String, String> map = load(f);
            for (Map.Entry<String, Integer> bc : baseCounts.entrySet()) {
                String key = bc.getKey();
                assertTrue("Missing key '" + key + "' in " + f.getName(), map.containsKey(key));
                int expected = bc.getValue();
                int actual = countPlaceholders(map.get(key));
                assertEquals("Placeholder count mismatch for key '" + key + "' in " + f.getName(), expected, actual);
            }
        }
    }

    private static int countPlaceholders(String v) {
        if (v == null || v.isEmpty()) return 0;
    int c = 0;
    int idx = 0;
        while ((idx = v.indexOf("%s", idx)) >= 0) {
            c++;
            idx += 2; // move past '%s'
        }
        return c;
    }

    private static Map<String, String> load(File f) throws java.io.IOException {
        Map<String, String> m = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (!t.isEmpty() && !t.startsWith("#")) {
                    int i = t.indexOf('=');
                    if (i > 0) {
                        String k = t.substring(0, i).trim();
                        String v = t.substring(i + 1).trim();
                        m.put(k, v);
                    }
                }
            }
        }
        return m;
    }
}
