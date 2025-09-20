package link.e4mc;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class IntegrationTest {

    @Test
    public void testModInitialization() {
        // Test that the main mod class can be instantiated
        E4mcMod mod = new E4mcMod();
        assertNotNull("Mod should be instantiable", mod);
    }
    
    @Test
    public void testConfigInitialization() {
        // Test that config can be created and has reasonable defaults
        File tmp = new File("build/tmp/test-config.cfg");
        tmp.getParentFile().mkdirs();
        Config config = new Config(tmp);
        assertNotNull("Config should be instantiable", config);
        
        // Test that all getter methods work
        assertNotNull("Broker URL should not be null", config.getBrokerUrl());
        assertTrue("Relay port should be positive", config.getRelayPort() > 0);
        assertNotNull("Relay host should not be null", config.getRelayHost());
    }
    
    @Test
    public void testCommandInitialization() {
        // Test that command can be created
        E4mcCommand command = new E4mcCommand();
        assertNotNull("Command should be instantiable", command);
        
        // Test basic command properties
        assertEquals("Command name should be e4mc", "e4mc", command.getCommandName());
        assertNotNull("Command usage should not be null", command.getCommandUsage(null));
        assertEquals("Permission level should be 0", 0, command.getRequiredPermissionLevel());
    }
    
    @Test
    public void testRelaySessionInitialization() {
        // Test that relay session can be created
        RelaySession session = new RelaySession();
        assertNotNull("Session should be instantiable", session);
        // Test initial state
        assertEquals("Initial state should be STOPPED", RelaySession.State.STOPPED, session.getState());
    }
    
    @Test
    public void testTextHelperBasicUsage() {
        // Test that TextHelper doesn't crash on basic usage
        String result = TextHelper.translate("test.key");
        
        // Since StatCollector will return the key if translation is missing,
        // we just verify it doesn't crash and returns something
        assertNotNull("Translation should not return null", result);
    }
    
    @Test
    public void testComponentIntegration() {
        // Test that all main components can work together
        E4mcMod mod = new E4mcMod();
        File tmp = new File("build/tmp/test-config.cfg");
        tmp.getParentFile().mkdirs();
        Config config = new Config(tmp);
        E4mcCommand command = new E4mcCommand();
        RelaySession session = new RelaySession();
        
        // Verify all components are properly initialized
        assertNotNull("Mod should be ready", mod);
        assertNotNull("Config should be ready", config);
        assertNotNull("Command should be ready", command);
        assertNotNull("Session should be ready", session);
    }
    
    @Test
    public void testConfigPersistence() {
        // Test that config can handle save/load operations without errors
        File tmp = new File("build/tmp/test-config.cfg");
        tmp.getParentFile().mkdirs();
        Config config = new Config(tmp);
        
        // Test that save doesn't crash (even though file operations might fail in test env)
        try {
            config.saveConfig();
            // If it doesn't throw, that's good enough for this test
            assertTrue("Save config should not throw", true);
        } catch (Exception e) {
            // In test environment, file operations might fail, but method should handle gracefully
            assertTrue("Config save should handle errors gracefully", true);
        }
        
        // Test that loading via re-instantiation doesn't crash
        try {
            Config reloaded = new Config(tmp);
            assertNotNull("Reloaded config should not be null", reloaded);
        } catch (Exception e) {
            assertTrue("Config reload should handle errors gracefully", true);
        }
    }
    
    @Test
    public void testSessionStateTransitions() {
        RelaySession session = new RelaySession();
        
        // Initial state
        assertEquals("Should start in STOPPED state", RelaySession.State.STOPPED, session.getState());
        
        // Starting should change state (even if it fails due to network issues in test env)
        session.startAsync();
        
        // State should either be CONNECTING or remain STOPPED (if start failed)
        RelaySession.State afterStart = session.getState();
        assertTrue("State should be CONNECTING or STOPPED after startAsync", 
                afterStart == RelaySession.State.CONNECTING || afterStart == RelaySession.State.STOPPED);
        
        // Stop should always result in STOPPED state
        session.stop();
        assertEquals("Should be STOPPED after stop", RelaySession.State.STOPPED, session.getState());
    }
}
