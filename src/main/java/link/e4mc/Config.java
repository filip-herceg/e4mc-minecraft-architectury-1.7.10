package link.e4mc;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    // Keys used in config file
    private static final String KEY_USE_BROKER = "useBroker";
    private static final String KEY_BROKER_URL = "brokerUrl";
    private static final String KEY_RELAY_HOST = "relayHost";
    private static final String KEY_RELAY_PORT = "relayPort";
    private static final String KEY_RESTORE_DEDICATED_CMDS = "restoreDedicatedCommands";
    private static final String KEY_USE_WHITELIST = "useWhiteList";
    // Prefer Forge Configuration when available, but fall back to Properties for tests/CI
    private Configuration forgeConfig;
    private final Properties props = new Properties();
    private final File file;
    private boolean useForge = true;

    // Backing fields with safe defaults (avoid public mutable statics)
    private boolean useBroker = true;
    private String brokerUrl = "https://broker.e4mc.link/getBestRelay";
    private String relayHost = "test.e4mc.link";
    private int relayPort = 25575;
    private boolean restoreDedicatedCommands = true;
    private boolean useWhiteList = false;

    public Config(File configFile) {
        this.file = configFile;
        if (configFile != null) {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                // Ensure parent directories exist for tests / CI
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
        }
        // Try to use Forge Configuration first
        try {
            forgeConfig = new Configuration(configFile);
            useForge = true;
            loadFromForge();
            // Ensure a config file is written so tests can assert file existence
            forgeConfig.save();
        } catch (Throwable t) {
            // Fallback to simple Properties if Forge config cannot be used in this environment
            useForge = false;
            loadFromProperties();
            savePropertiesQuietly();
        }
    }

    private void loadFromForge() {
        try {
            forgeConfig.load();
        useBroker = forgeConfig.getBoolean(KEY_USE_BROKER, Configuration.CATEGORY_GENERAL, useBroker,
                    "Whether to use the broker to get the best relay based on location or use a hard-coded relay.");

        brokerUrl = forgeConfig.getString(KEY_BROKER_URL, Configuration.CATEGORY_GENERAL, brokerUrl,
                    "URL for the broker service");

        relayHost = forgeConfig.getString(KEY_RELAY_HOST, Configuration.CATEGORY_GENERAL, relayHost,
                    "Default relay host to use when broker is disabled");

            int min = 1;
            int max = 65535;
            relayPort = forgeConfig.getInt(KEY_RELAY_PORT, Configuration.CATEGORY_GENERAL, relayPort, min, max,
                    "Port for the relay service");

            restoreDedicatedCommands = forgeConfig.getBoolean(KEY_RESTORE_DEDICATED_CMDS, Configuration.CATEGORY_GENERAL, restoreDedicatedCommands,
                    "Allows use of certain dedicated server commands such as /ban and /whitelist");

            useWhiteList = forgeConfig.getBoolean(KEY_USE_WHITELIST, Configuration.CATEGORY_GENERAL, useWhiteList,
                    "Whether to use whitelists on LAN worlds");
        } catch (Throwable ignored) {
            // Keep defaults
        } finally {
            try { 
                // Persist current values to ensure file exists for tests
                forgeConfig.save(); 
            } catch (Throwable ignored2) {
                // Ignore save failures in headless test/CI environment
            }
        }
    }

    private void loadFromProperties() {
        if (file != null && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException ignored) {
                // Use defaults if cannot read; file might not exist yet in tests
            }
        }
        // Populate defaults into fields, honoring any user-set values
    useBroker = parseBoolean(props.getProperty(KEY_USE_BROKER), true);
    brokerUrl = props.getProperty(KEY_BROKER_URL, brokerUrl);
    relayHost = props.getProperty(KEY_RELAY_HOST, relayHost);
    relayPort = parseIntInRange(props.getProperty(KEY_RELAY_PORT), 25575, 1, 65535);
    restoreDedicatedCommands = parseBoolean(props.getProperty(KEY_RESTORE_DEDICATED_CMDS), true);
    useWhiteList = parseBoolean(props.getProperty(KEY_USE_WHITELIST), false);

        // Ensure props have defaults written for visibility
    props.setProperty(KEY_USE_BROKER, String.valueOf(useBroker));
    props.setProperty(KEY_BROKER_URL, brokerUrl);
    props.setProperty(KEY_RELAY_HOST, relayHost);
    props.setProperty(KEY_RELAY_PORT, String.valueOf(relayPort));
    props.setProperty(KEY_RESTORE_DEDICATED_CMDS, String.valueOf(restoreDedicatedCommands));
    props.setProperty(KEY_USE_WHITELIST, String.valueOf(useWhiteList));
    }

    private static boolean parseBoolean(String value, boolean def) {
        if (value == null) return def;
        return Boolean.parseBoolean(value);
    }

    private static int parseIntInRange(String value, int def, int min, int max) {
        try {
            int v = Integer.parseInt(value);
            if (v < min || v > max) return def;
            return v;
        } catch (Exception e) {
            return def;
        }
    }

    private void savePropertiesQuietly() {
        if (file == null) return;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "e4mc configuration");
        } catch (IOException ignored) {
            // Ignore write failures in CI/test where FS may be ephemeral
        }
    }

    public void saveConfig() {
        if (useForge && forgeConfig != null) {
            forgeConfig.save();
        } else {
            savePropertiesQuietly();
        }
    }

    // Getters
    public boolean isUseBroker() { return useBroker; }
    public String getBrokerUrl() { return brokerUrl; }
    public String getRelayHost() { return relayHost; }
    public int getRelayPort() { return relayPort; }
    public boolean isRestoreDedicatedCommands() { return restoreDedicatedCommands; }
    public boolean isUseWhiteList() { return useWhiteList; }

    // Setters that update both memory and the underlying configuration file
    public void setUseBroker(boolean value) {
        this.useBroker = value;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_USE_BROKER, true).set(value);
        } else {
            props.setProperty(KEY_USE_BROKER, String.valueOf(value));
        }
        saveConfig();
    }

    public void setBrokerUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return; // ignore invalid input to keep previous value
        }
        this.brokerUrl = url;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_BROKER_URL, "https://broker.e4mc.link/getBestRelay").set(url);
        } else {
            props.setProperty(KEY_BROKER_URL, url);
        }
        saveConfig();
    }

    public void setRelayHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return; // ignore invalid input to keep previous value
        }
        this.relayHost = host;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_RELAY_HOST, "test.e4mc.link").set(host);
        } else {
            props.setProperty(KEY_RELAY_HOST, host);
        }
        saveConfig();
    }

    public void setRelayPort(int port) {
        if (port < 1 || port > 65535) {
            return; // ignore invalid input
        }
        this.relayPort = port;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_RELAY_PORT, 25575).set(port);
        } else {
            props.setProperty(KEY_RELAY_PORT, String.valueOf(port));
        }
        saveConfig();
    }

    public void setRestoreDedicatedCommands(boolean value) {
        this.restoreDedicatedCommands = value;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_RESTORE_DEDICATED_CMDS, true).set(value);
        } else {
            props.setProperty(KEY_RESTORE_DEDICATED_CMDS, String.valueOf(value));
        }
        saveConfig();
    }

    public void setUseWhiteList(boolean value) {
        this.useWhiteList = value;
        if (useForge && forgeConfig != null) {
            forgeConfig.get(Configuration.CATEGORY_GENERAL, KEY_USE_WHITELIST, false).set(value);
        } else {
            props.setProperty(KEY_USE_WHITELIST, String.valueOf(value));
        }
        saveConfig();
    }
}