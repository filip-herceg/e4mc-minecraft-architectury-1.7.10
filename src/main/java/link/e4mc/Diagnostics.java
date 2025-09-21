package link.e4mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Simple runtime diagnostics toggles and helpers. */
public final class Diagnostics {
    private static final Logger LOGGER = LogManager.getLogger("e4mc-diagnostics");
    private static volatile boolean enabled = false;

    private Diagnostics() { }

    public static void setEnabled(boolean value) {
        enabled = value;
        LOGGER.info("Diagnostics {}", value ? "ENABLED" : "DISABLED");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void info(String msg) {
        if (enabled) LOGGER.info(msg);
    }

    public static void info(String fmt, Object arg) {
        if (enabled) LOGGER.info(fmt, arg);
    }

    public static void info(String fmt, Object a, Object b) {
        if (enabled) LOGGER.info(fmt, a, b);
    }

    public static void info(String fmt, Object... args) {
        if (enabled) LOGGER.info(fmt, args);
    }

    public static void debug(String fmt, Object a) {
        if (enabled) LOGGER.debug(fmt, a);
    }

    public static void debug(String fmt, Object... args) {
        if (enabled) LOGGER.debug(fmt, args);
    }
}
