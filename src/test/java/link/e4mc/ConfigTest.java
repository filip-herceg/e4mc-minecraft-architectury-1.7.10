package link.e4mc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import link.e4mc.util.FilesEx;

public class ConfigTest {
    private File tempConfigFile;
    private Config config;

    /**
     * Custom exception for ConfigTest-specific errors
     */
    public static class ConfigTestException extends Exception {
        public ConfigTestException(String message) {
            super(message);
        }
        
        public ConfigTestException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    @Before
    public void setUp() throws IOException {
        // Create a temporary file for testing
        tempConfigFile = File.createTempFile("e4mc_test_config", ".cfg");
        tempConfigFile.deleteOnExit();
    }
    
    @After
    public void tearDown() throws ConfigTestException {
        if (tempConfigFile != null && tempConfigFile.exists()) {
            try {
                FilesEx.delete(tempConfigFile.toPath());
            } catch (IOException e) {
                throw new ConfigTestException("Failed to delete temporary config file: " + tempConfigFile.getAbsolutePath(), e);
            }
        }
    }
    
    @Test
    public void testConfigDefaultValues() {
        config = new Config(tempConfigFile);
        
        // Test default values
        assertTrue("useBroker should default to true", config.isUseBroker());
        assertEquals("brokerUrl should have default value", 
                "https://broker.e4mc.link/getBestRelay", config.getBrokerUrl());
        assertEquals("relayHost should have default value", 
                "test.e4mc.link", config.getRelayHost());
        assertEquals("relayPort should default to 25575", 25575, config.getRelayPort());
        assertTrue("restoreDedicatedCommands should default to true", config.isRestoreDedicatedCommands());
        assertFalse("useWhiteList should default to false", config.isUseWhiteList());
    }
    
    @Test
    public void testConfigFileCreation() {
        config = new Config(tempConfigFile);
        assertTrue("Config file should be created", tempConfigFile.exists());
        assertTrue("Config file should not be empty", tempConfigFile.length() > 0);
    }
    
    @Test
    public void testConfigPersistence() {
        // Create config with default values
        config = new Config(tempConfigFile);
        
        // Modify values
        config.setUseBroker(false);
        config.setRelayHost("custom.relay.host");
        config.setRelayPort(12345);

        // Create new config instance from same file
        Config loadedConfig = new Config(tempConfigFile);
        
        // Verify values were persisted
        assertFalse("useBroker should be persisted as false", loadedConfig.isUseBroker());
        assertEquals("relayHost should be persisted", "custom.relay.host", loadedConfig.getRelayHost());
        assertEquals("relayPort should be persisted", 12345, loadedConfig.getRelayPort());
    }
    
    @Test
    public void testConfigValidation() {
        config = new Config(tempConfigFile);
        
        // Test port range validation (this would be enforced by the Configuration class)
        assertTrue("Port should be in valid range", config.getRelayPort() >= 1 && config.getRelayPort() <= 65535);
        
        // Test that URLs are strings
        assertNotNull("brokerUrl should not be null", config.getBrokerUrl());
        assertNotNull("relayHost should not be null", config.getRelayHost());
        
        // Test that broker URL is a valid format (basic check)
        assertTrue("brokerUrl should start with http", 
                config.getBrokerUrl().startsWith("http://") || config.getBrokerUrl().startsWith("https://"));
    }
}
