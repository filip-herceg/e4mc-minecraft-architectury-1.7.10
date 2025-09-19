package link.e4mc;

public class E4mcClient {
    public static final String MOD_ID = "e4mc_minecraft";
    public static RelaySession session; // Kept non-final as it needs to be reassigned
    
    private E4mcClient() {
        // Utility class
    }
    
    public static void init() {
        // Initialization handled by the main mod class
        E4mcMod.getLogger().info("e4mc client initialized");
    }
}
