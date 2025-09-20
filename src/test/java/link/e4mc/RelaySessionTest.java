package link.e4mc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RelaySessionTest {
    private RelaySession relaySession;

    @Before
    public void setUp() {
        relaySession = new RelaySession();
    }

    @Test
    public void testInitialState() {
        assertEquals("Initial state should be STOPPED", RelaySession.State.STOPPED, relaySession.getState());
    }

    @Test
    public void testStartAsync() {
        relaySession.startAsync();
        RelaySession.State state = relaySession.getState();
        assertTrue("Should be CONNECTING or STOPPED after startAsync", 
                state == RelaySession.State.CONNECTING || state == RelaySession.State.STOPPED);
    }

    @Test
    public void testStopWhenStopped() {
        assertEquals("Initial state should be STOPPED", RelaySession.State.STOPPED, relaySession.getState());
        relaySession.stop();
        assertEquals("State should remain STOPPED", RelaySession.State.STOPPED, relaySession.getState());
    }

    @Test
    public void testMultipleStops() {
        relaySession.stop();
        relaySession.stop();
        relaySession.stop();
        assertEquals("State should remain STOPPED after multiple stops", 
                RelaySession.State.STOPPED, relaySession.getState());
    }
}
