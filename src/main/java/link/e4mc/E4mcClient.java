package link.e4mc;

public final class E4mcClient {
    public static final String MOD_ID = "e4mc_minecraft";

    private static RelaySession session; // mutable by design, accessed via getters/setters

    private E4mcClient() {
        // Prevent instantiation
        throw new AssertionError("No instances of E4mcClient");
    }

    public static void init() {
        // Initialization handled by the main mod class
        E4mcMod.getLogger().info("e4mc client initialized");
    }

    public static RelaySession getSession() {
        return session;
    }

    public static void setSession(RelaySession newSession) {
        session = newSession;
    }

    // Small helpers to reduce null checks sprinkled across the code
    public static boolean hasActiveSession() {
        return session != null && session.getState() != RelaySession.State.STOPPED;
    }

    public static void ensureSessionStarted() {
        if (session == null) {
            session = new RelaySession();
            session.startAsync();
        }
    }
}
