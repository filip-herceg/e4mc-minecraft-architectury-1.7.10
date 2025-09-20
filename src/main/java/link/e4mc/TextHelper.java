package link.e4mc;

import net.minecraft.util.StatCollector;

public class TextHelper {
    
    private TextHelper() {
        // Utility class
    }
    
    public static String translate(String key, Object... args) {
        if (key == null) {
            return ""; // handle null keys gracefully
        }
        if (args == null || args.length == 0) {
            return StatCollector.translateToLocal(key);
        }
        return StatCollector.translateToLocalFormatted(key, args);
    }
    
    public static String translate(String key) {
        if (key == null) {
            return "";
        }
        return StatCollector.translateToLocal(key);
    }
}
