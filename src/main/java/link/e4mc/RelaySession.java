package link.e4mc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelaySession {
    private static final Logger LOGGER = LogManager.getLogger(RelaySession.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Socket relaySocket;
    private State state = State.STOPPED;

    // Diagnostics
    private String lastSource = "none"; // broker|fallback|default
    private String lastRelayId = "";
    private String lastRelayHost = "";
    private int lastRelayPort = -1;
    private String lastBrokerUrl = "";
    private int lastHttpCode = -1;
    private String lastHttpSnippet = "";
    private String lastError = "";

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
        Thread thread = new Thread(this::start, "e4mc-relay-session");
        thread.setDaemon(true);
        thread.start();
    }

    private void start() {
        if (running.get()) {
            return;
        }
        running.set(true);
        state = State.CONNECTING;
        Diagnostics.info("Session starting (thread={})", Thread.currentThread().getName());
        try {
            BrokerResponse relayInfo = getBrokerInfo();
            if (relayInfo == null) {
                state = State.UNHEALTHY;
                addChatMessage(EnumChatFormatting.RED + "Failed to get relay information");
                return;
            }
            lastRelayId = relayInfo.getId();
            lastRelayHost = relayInfo.getHost();
            lastRelayPort = relayInfo.getPort();
            Diagnostics.info("Connecting to relay {} ({}:{}) via {}", lastRelayId, lastRelayHost, lastRelayPort, lastSource);
            Socket s = new Socket();
            s.connect(new InetSocketAddress(relayInfo.getHost(), relayInfo.getPort()), 5000);
            s.setSoTimeout(10000);
            relaySocket = s;
            state = State.STARTED;
            addChatMessage(EnumChatFormatting.GREEN + "Connected to relay: " + relayInfo.getId());
            Thread readerThread = new Thread(this::handleIncomingData, "e4mc-relay-reader");
            readerThread.setDaemon(true);
            readerThread.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start relay session", e);
            lastError = e.toString();
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
            Config cfg = E4mcMod.getConfig();
            if (cfg == null) {
                BrokerResponse r = new BrokerResponse();
                r.setId("default");
                r.setHost("127.0.0.1");
                r.setPort(25565);
                lastSource = "default";
                return r;
            }

            if (cfg.isUseBroker()) {
                String url = cfg.getBrokerUrl();
                lastBrokerUrl = url;
                String body = Http.get(url, 3000);
                lastHttpCode = Http.lastCode();
                lastHttpSnippet = body != null ? body.substring(0, Math.min(120, body.length())) : "";
                BrokerResponse r = parseBrokerResponse(body);
                if (r != null) { lastSource = "broker"; return r; }
                LOGGER.warn("Broker response invalid, falling back to configured relay host/port");
            }

            BrokerResponse r = new BrokerResponse();
            r.setId("configured");
            r.setHost(cfg.getRelayHost());
            r.setPort(cfg.getRelayPort());
            lastSource = "fallback";
            return r;
        } catch (Exception e) {
            LOGGER.error("Failed to get broker info", e);
            lastError = e.toString();
            return null;
        }
    }

    private BrokerResponse parseBrokerResponse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            String s = json.replace('\n', ' ').replace('\r', ' ').trim();
            BrokerResponse r = new BrokerResponse();
            String id = extractJsonString(s, "\"id\"");
            String host = extractJsonString(s, "\"host\"");
            Integer port = extractJsonInt(s, "\"port\"");
            if (host == null || port == null) return null;
            r.setId(id != null ? id : "");
            r.setHost(host);
            r.setPort(port);
            return r;
        } catch (Exception e) {
            LOGGER.warn("Failed to parse broker response: {}", e.toString());
            return null;
        }
    }

    private static String extractJsonString(String src, String key) {
        int i = src.indexOf(key);
        if (i < 0) return null;
        int colon = src.indexOf(':', i + key.length());
        if (colon < 0) return null;
        int q1 = src.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = src.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return src.substring(q1 + 1, q2);
    }

    private static Integer extractJsonInt(String src, String key) {
        int i = src.indexOf(key);
        if (i < 0) return null;
        int colon = src.indexOf(':', i + key.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < src.length() && Character.isWhitespace(src.charAt(start))) start++;
        int end = start;
        while (end < src.length() && Character.isDigit(src.charAt(end))) end++;
        if (end == start) return null;
        try { return Integer.parseInt(src.substring(start, end)); } catch (NumberFormatException e) { return null; }
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

    // Minimal HTTP utility to fetch broker JSON using JRE classes
    private static final class Http {
        private static int code = -1;
        static int lastCode() { return code; }
        static String get(String url, int timeoutMs) throws IOException {
            java.net.URL u = java.net.URI.create(url).toURL();
            java.net.URLConnection c = u.openConnection();
            if (c instanceof java.net.HttpURLConnection) {
                java.net.HttpURLConnection h = (java.net.HttpURLConnection) c;
                h.setConnectTimeout(timeoutMs);
                h.setReadTimeout(timeoutMs);
                h.setRequestMethod("GET");
                h.setRequestProperty("Accept", "application/json");
                code = h.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? h.getInputStream() : h.getErrorStream();
                if (is == null) return null;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    return sb.toString();
                }
            } else {
                code = 0;
                try (InputStream is = c.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    return sb.toString();
                }
            }
        }
    }

    // === Diagnostics getters ===
    public String getLastSource() { return lastSource; }
    public String getLastRelayId() { return lastRelayId; }
    public String getLastRelayHost() { return lastRelayHost; }
    public int getLastRelayPort() { return lastRelayPort; }
    public String getLastBrokerUrl() { return lastBrokerUrl; }
    public int getLastHttpCode() { return lastHttpCode; }
    public String getLastHttpSnippet() { return lastHttpSnippet; }
    public String getLastError() { return lastError; }
}