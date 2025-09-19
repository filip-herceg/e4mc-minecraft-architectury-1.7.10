package link.e4mc;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

@Mod(modid = E4mcMod.MODID, name = E4mcMod.NAME, version = E4mcMod.VERSION)
public class E4mcMod {
    public static final String MODID = "e4mc_minecraft";
    public static final String NAME = "e4mc";
    public static final String VERSION = "5.4.1-1.7.10";
    
    private static Logger logger;
    private static Config config;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new Config(event.getSuggestedConfigurationFile());
        
        // Register event handler
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Register commands
        event.registerServerCommand(new E4mcCommand());
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static Config getConfig() {
        return config;
    }
}
