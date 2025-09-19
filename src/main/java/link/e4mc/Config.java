package link.e4mc;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    private final Configuration configuration;

    // Backing fields (avoid public mutable statics)
    private boolean useBroker;
    private String brokerUrl;
    private String relayHost;
    private int relayPort;
    private boolean restoreDedicatedCommands;
    private boolean useWhiteList;

    public Config(File configFile) {
        configuration = new Configuration(configFile);
        loadConfig();
    }

    private void loadConfig() {
        try {
            configuration.load();
            useBroker = configuration.getBoolean("useBroker", Configuration.CATEGORY_GENERAL, true,
                    "Whether to use the broker to get the best relay based on location or use a hard-coded relay.");

            brokerUrl = configuration.getString("brokerUrl", Configuration.CATEGORY_GENERAL, "https://broker.e4mc.link/getBestRelay",
                    "URL for the broker service");

            relayHost = configuration.getString("relayHost", Configuration.CATEGORY_GENERAL, "test.e4mc.link",
                    "Default relay host to use when broker is disabled");

            int defaultPort = 25575;
            int min = 1;
            int max = 65535;
            relayPort = configuration.getInt("relayPort", Configuration.CATEGORY_GENERAL, defaultPort, min, max,
                    "Port for the relay service");

            restoreDedicatedCommands = configuration.getBoolean("restoreDedicatedCommands", Configuration.CATEGORY_GENERAL, true,
                    "Allows use of certain dedicated server commands such as /ban and /whitelist");

            useWhiteList = configuration.getBoolean("useWhiteList", Configuration.CATEGORY_GENERAL, false,
                    "Whether to use whitelists on LAN worlds");
        } finally {
            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
    }

    public void saveConfig() {
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public boolean isUseBroker() { return useBroker; }
    public String getBrokerUrl() { return brokerUrl; }
    public String getRelayHost() { return relayHost; }
    public int getRelayPort() { return relayPort; }
    public boolean isRestoreDedicatedCommands() { return restoreDedicatedCommands; }
    public boolean isUseWhiteList() { return useWhiteList; }
}