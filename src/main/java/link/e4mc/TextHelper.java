package link.e4mc;

import net.minecraft.util.StatCollector;

public class TextHelper {
    
    private TextHelper() {
        // Utility class
    }
    
    public static String translate(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }
    
    public static String translate(String key) {
        return StatCollector.translateToLocal(key);
    }
}
