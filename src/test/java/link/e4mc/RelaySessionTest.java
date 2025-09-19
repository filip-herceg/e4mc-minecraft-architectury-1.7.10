package link.e4mc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RelaySessionTest {
    private static final String TEST_SERVER_ADDRESS = "localhost:25565";
    private static final String TEST_BROKER_URL = "http://test-broker.com";
    
    private RelaySession relaySession;
    
    @Mock
    private Config mockConfig;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Setup config mocks
        when(mockConfig.getServerAddress()).thenReturn(TEST_SERVER_ADDRESS);
        when(mockConfig.getBrokerUrl()).thenReturn(TEST_BROKER_URL);
        when(mockConfig.getPort()).thenReturn(25565);
        when(mockConfig.getAddress()).thenReturn("localhost");
        
        relaySession = new RelaySession(mockConfig);
    }
    
    @Test
    public void testInitialState() {
        assertEquals("Initial state should be STOPPED", RelaySession.State.STOPPED, relaySession.getState());
        assertNull("Domain should be null initially", relaySession.getDomain());
    }
    
    @Test
    public void testGetServerAddressFromConfig() {
        String serverAddress = relaySession.getServerAddress();
        assertEquals("Should return server address from config", TEST_SERVER_ADDRESS, serverAddress);
        
        // Verify config was accessed
        verify(mockConfig, atLeastOnce()).getServerAddress();
    }
    
    @Test
    public void testStartAsyncCallsConfig() {
        // Starting should access the broker URL from config
        relaySession.startAsync();
        
        // Verify config methods were called
        verify(mockConfig, atLeastOnce()).getBrokerUrl();
        verify(mockConfig, atLeastOnce()).getServerAddress();
    }
    
    @Test
    public void testStopWhenStopped() {
        // Session is already stopped
        assertEquals("Initial state should be STOPPED", RelaySession.State.STOPPED, relaySession.getState());
        
        relaySession.stop();
        
        // Should remain stopped
        assertEquals("State should remain STOPPED", RelaySession.State.STOPPED, relaySession.getState());
    }
    
    @Test
    public void testMultipleStops() {
        relaySession.stop();
        relaySession.stop();
        relaySession.stop();
        
        // Should handle multiple stops gracefully
        assertEquals("State should remain STOPPED after multiple stops", 
                RelaySession.State.STOPPED, relaySession.getState());
    }
    
    @Test
    public void testToString() {
        String stringRepresentation = relaySession.toString();
        
        assertNotNull("ToString should not be null", stringRepresentation);
        assertTrue("ToString should contain state", stringRepresentation.contains("STOPPED"));
        assertTrue("ToString should contain server address", stringRepresentation.contains(TEST_SERVER_ADDRESS));
    }
    
    @Test
    public void testStateEnumValues() {
        // Test that all expected states exist
        RelaySession.State[] states = RelaySession.State.values();
        
        assertTrue("Should have at least 3 states", states.length >= 3);
        
        boolean hasStarting = false;
        boolean hasStarted = false;
        boolean hasStopped = false;
        
        for (RelaySession.State state : states) {
            if (state == RelaySession.State.STARTING) hasStarting = true;
            if (state == RelaySession.State.STARTED) hasStarted = true;
            if (state == RelaySession.State.STOPPED) hasStopped = true;
        }
        
        assertTrue("Should have STARTING state", hasStarting);
        assertTrue("Should have STARTED state", hasStarted);
        assertTrue("Should have STOPPED state", hasStopped);
    }
    
    @Test
    public void testStartAsyncWithNullConfig() {
        // Test with null config values
        when(mockConfig.getBrokerUrl()).thenReturn(null);
        when(mockConfig.getServerAddress()).thenReturn(null);
        
        RelaySession nullConfigSession = new RelaySession(mockConfig);
        
        // Should handle null config values gracefully
        nullConfigSession.startAsync();
        
        // Should remain in initial state or handle error gracefully
        RelaySession.State state = nullConfigSession.getState();
        assertTrue("Should handle null config gracefully", 
                state == RelaySession.State.STOPPED || state == RelaySession.State.STARTING);
    }
    
    @Test
    public void testConfigUsage() {
        // Verify that RelaySession properly uses all expected config methods
        
        // These calls should trigger config access
        relaySession.getServerAddress();
        relaySession.startAsync();
        
        // Verify expected config methods were called
        verify(mockConfig, atLeastOnce()).getServerAddress();
        verify(mockConfig, atLeastOnce()).getBrokerUrl();
    }
}
