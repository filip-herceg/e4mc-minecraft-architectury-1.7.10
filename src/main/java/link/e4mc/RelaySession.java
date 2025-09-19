package link.e4mc;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelaySession {
    private static final Logger LOGGER = LogManager.getLogger(RelaySession.class);
    private static final Gson GSON = new Gson();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Socket relaySocket;
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
            BrokerResponse relayInfo = getBrokerInfo();
            if (relayInfo == null) {
                state = State.UNHEALTHY;
                addChatMessage(EnumChatFormatting.RED + "Failed to get relay information");
                return;
            }
            relaySocket = new Socket(relayInfo.getHost(), relayInfo.getPort());
            state = State.STARTED;
            addChatMessage(EnumChatFormatting.GREEN + "Connected to relay: " + relayInfo.getId());
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(relaySocket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && running.get()) {
                LOGGER.debug("Received from relay: {}", line);
            }
        } catch (IOException e) {
            if (running.get()) {
                LOGGER.error("Error reading from relay", e);
                state = State.UNHEALTHY;
                addChatMessage(EnumChatFormatting.RED + "Lost connection to relay");
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
            BrokerResponse response = new BrokerResponse();
            response.setId("test-relay");
            response.setHost("localhost");
            response.setPort(25565);
            return response;
        } catch (Exception e) {
            LOGGER.error("Failed to get broker info", e);
            return null;
        }
    }

    private void addChatMessage(String message) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(message));
            }
        } catch (Exception e) {
            LOGGER.info("Chat message: {}", message);
        }
    }

    private static final class BrokerResponse {
        private String id;
        private String host;
        private int port;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }
}