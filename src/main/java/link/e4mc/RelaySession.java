package link.e4mc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simplified networking session for 1.7.10 - replaces QUIC with basic TCP sockets
 * Compatible with Java 8 and Minecraft 1.7.10 Forge
 */
public class RelaySession {
    private static final Logger LOGGER = LogManager.getLogger(RelaySession.class);
    private static final Gson gson = new Gson();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Socket relaySocket;
    private String assignedDomain;
    private State state = State.STOPPED;
    
    public enum State {
        STOPPED,
        CONNECTING,
        STARTED,
        UNHEALTHY
    }
    
    public State getState() {
        return state;
    }
    
    public void startAsync() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, "e4mc-relay-session");
        thread.setDaemon(true);
        thread.start();
    }
    
    private void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        state = State.CONNECTING;
        
        try {
            // Get broker info for relay
            BrokerResponse relayInfo = getBrokerInfo();
            if (relayInfo == null) {
                state = State.UNHEALTHY;
                addChatMessage(EnumChatFormatting.RED + "Failed to get relay information");
                return;
            }
            
            // Connect to relay server
            relaySocket = new Socket(relayInfo.host, relayInfo.port);
            state = State.STARTED;
            
            addChatMessage(EnumChatFormatting.GREEN + "Connected to relay: " + relayInfo.id);
            
            // Handle incoming data in separate thread
            Thread readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleIncomingData();
                }
            }, "e4mc-relay-reader");
            readerThread.setDaemon(true);
            readerThread.start();
            
        } catch (Exception e) {
            LOGGER.error("Failed to start relay session", e);
            state = State.UNHEALTHY;
            addChatMessage(EnumChatFormatting.RED + "Failed to connect to relay: " + e.getMessage());
        }
    }
    
    private void handleIncomingData() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(relaySocket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && running.get()) {
                // Process incoming relay messages
                LOGGER.debug("Received from relay: {}", line);
                // Handle different message types here
            }
        } catch (IOException e) {
            if (running.get()) {
                LOGGER.error("Error reading from relay", e);
                state = State.UNHEALTHY;
                addChatMessage(EnumChatFormatting.RED + "Lost connection to relay");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.warn("Error closing reader", e);
                }
            }
        }
    }
    
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        state = State.STOPPED;
        
        if (relaySocket != null && !relaySocket.isClosed()) {
            try {
                relaySocket.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing relay socket", e);
            }
        }
        
        addChatMessage(EnumChatFormatting.YELLOW + "Disconnected from relay");
    }
    
    private BrokerResponse getBrokerInfo() {
        try {
            // For now, use a simple test configuration
            // In production, this would query the actual broker service
            BrokerResponse response = new BrokerResponse();
            response.id = "test-relay";
            response.host = "localhost";
            response.port = 25565; // Default Minecraft port for testing
            return response;
            
        } catch (Exception e) {
            LOGGER.error("Failed to get broker info", e);
            return null;
        }
    }
    
    private void addChatMessage(String message) {
        // For Minecraft 1.7.10, we need to check if we're on the client side
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                ChatComponentText chatMessage = new ChatComponentText(message);
                mc.thePlayer.addChatMessage(chatMessage);
            }
        } catch (Exception e) {
            // Ignore if we're on server side or Minecraft isn't available
            LOGGER.info("Chat message: {}", message);
        }
    }
    
    // Simple data classes for JSON responses
    private static class BrokerResponse {
        public String id;
        public String host;
        public int port;
    }
    
    // Control message classes for future extensions
    private interface ControlMessage {
    }
    
    private static class RequestDomainAssignmentMessage implements ControlMessage {
        @SuppressWarnings("unused")
        private final String kind = "request_domain_assignment";
    }
}