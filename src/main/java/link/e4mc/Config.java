package link.e4mc;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    private Configuration configuration;
    
    // Config values - kept public for simple access as they're config values
    public boolean useBroker;
    public String brokerUrl;
    public String relayHost;
    public int relayPort;
    public boolean restoreDedicatedCommands;
    public boolean useWhiteList;
    
    public Config(File configFile) {
        configuration = new Configuration(configFile);
        loadConfig();
    }
    
    private void loadConfig() {
        try {
            configuration.load();
            
            // Load config values with defaults
            useBroker = configuration.getBoolean("useBroker", Configuration.CATEGORY_GENERAL, true,
                    "Whether to use the broker to get the best relay based on location or use a hard-coded relay.");
            
            brokerUrl = configuration.getString("brokerUrl", Configuration.CATEGORY_GENERAL, "https://broker.e4mc.link/getBestRelay",
                    "URL for the broker service");
            
            relayHost = configuration.getString("relayHost", Configuration.CATEGORY_GENERAL, "test.e4mc.link",
                    "Default relay host to use when broker is disabled");
            
            relayPort = configuration.getInt("relayPort", Configuration.CATEGORY_GENERAL, 25575, 1, 65535,
                    "Port for the relay service");
            
            restoreDedicatedCommands = configuration.getBoolean("restoreDedicatedCommands", Configuration.CATEGORY_GENERAL, true,
                    "Allows use of certain dedicated server commands such as /ban and /whitelist");
            
            useWhiteList = configuration.getBoolean("useWhiteList", Configuration.CATEGORY_GENERAL, false,
                    "Whether to use whitelists on LAN worlds");
            
        } catch (Exception e) {
            E4mcMod.getLogger().error("Problem loading config file!", e);
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
}