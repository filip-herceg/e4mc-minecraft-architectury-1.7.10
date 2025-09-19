package link.e4mc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simplified networking session for 1.7.10 - replaces QUIC with basic TCP sockets
 */
public class RelaySession {
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
        CompletableFuture.runAsync(this::start);
    }
    
    private void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        state = State.CONNECTING;
        
        try {
            String relayHost;
            int relayPort;
            
            Config config = E4mcMod.getConfig();
            if (config.useBroker) {
                String brokerResponse = getBrokerResponse();
                JsonObject brokerData = gson.fromJson(brokerResponse, JsonObject.class);
                relayHost = brokerData.get("host").getAsString();
                relayPort = brokerData.get("port").getAsInt();
            } else {
                relayHost = config.relayHost;
                relayPort = config.relayPort;
            }
            
            // Connect to relay
            relaySocket = new Socket(relayHost, relayPort);
            
            // Send domain assignment request
            sendControlMessage(new RequestDomainAssignmentMessage());
            
            // Start relay thread
            Thread relayThread = new Thread(this::handleRelay);
            relayThread.setDaemon(true);
            relayThread.start();
            
            state = State.STARTED;
            E4mcMod.getLogger().info("e4mc relay session started");
            
        } catch (Exception e) {
            E4mcMod.getLogger().error("Failed to start e4mc relay", e);
            state = State.UNHEALTHY;
            stop();
        }
    }
    
    public void stop() {
        running.set(false);
        state = State.STOPPED;
        
        if (relaySocket != null && !relaySocket.isClosed()) {
            try {
                relaySocket.close();
            } catch (IOException e) {
                E4mcMod.getLogger().warn("Error closing relay socket", e);
            }
        }
    }
    
    private String getBrokerResponse() throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(E4mcMod.getConfig().brokerUrl);
        HttpResponse response = client.execute(request);
        return EntityUtils.toString(response.getEntity());
    }
    
    private void sendControlMessage(ControlMessage message) throws IOException {
        String json = gson.toJson(message);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        
        DataOutputStream out = new DataOutputStream(relaySocket.getOutputStream());
        out.writeByte(data.length);
        out.write(data);
        out.flush();
    }
    
    private void handleRelay() {
        try (DataInputStream in = new DataInputStream(relaySocket.getInputStream())) {
            while (running.get() && !relaySocket.isClosed()) {
                int messageLength = in.readByte() & 0xFF;
                byte[] messageData = new byte[messageLength];
                in.readFully(messageData);
                
                String jsonString = new String(messageData, StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(jsonString, JsonObject.class);
                
                String messageType = json.get("kind").getAsString();
                if ("domain_assignment_complete".equals(messageType)) {
                    assignedDomain = json.get("domain").getAsString();
                    notifyDomainAssigned();
                } else if ("request_message_broadcast".equals(messageType)) {
                    String message = json.get("message").getAsString();
                    broadcastMessage(message);
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                E4mcMod.getLogger().error("Relay connection error", e);
                state = State.UNHEALTHY;
            }
        }
    }
    
    private void notifyDomainAssigned() {
        // Notify in chat if we're on client side
        if (Minecraft.getMinecraft() != null) {
            String message = TextHelper.translate("text.e4mc_minecraft.domainAssigned", assignedDomain);
            ChatComponentText chatMessage = new ChatComponentText(EnumChatFormatting.GREEN + message);
            Minecraft.getMinecraft().thePlayer.addChatMessage(chatMessage);
        }
    }
    
    private void broadcastMessage(String message) {
        // Handle message broadcasting
        if (Minecraft.getMinecraft() != null) {
            ChatComponentText chatMessage = new ChatComponentText(message);
            Minecraft.getMinecraft().thePlayer.addChatMessage(chatMessage);
        }
    }
    
    // Control message classes
    private interface ControlMessage {
    }
    
    private static class RequestDomainAssignmentMessage implements ControlMessage {
        private final String kind = "request_domain_assignment";
    }
}

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.read();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            toQuiclime.writeAndFlush(msg).addListener(it -> {
                if (it.isSuccess()) {
                    ctx.channel().read();
                } else {
                    QuiclimeSession.this.state = State.UNHEALTHY;
                    if (Agnos.isClient()) {
                        Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                    }
                    toQuiclime.close();
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            LOGGER.info("channel inactive(from MC): {} (MC: {})", toQuiclime, ctx.channel());
            if (toQuiclime.isActive()) {
                toQuiclime.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            QuiclimeSession.this.state = State.UNHEALTHY;
            if (Agnos.isClient()) {
                Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
            }
            this.channelInactive(ctx);
        }
    }

    private class ToQuiclimeHandler extends ChannelInboundHandlerAdapter {
        LocalChannel toMinecraft;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            LOGGER.info("channel active: {}", ctx.channel());
            var fut = new Bootstrap()
                    .group(ctx.channel().eventLoop())
                    .channel(LocalChannel.class)
                    .handler(new ToMinecraftHandler((QuicStreamChannel) ctx.channel()))
                    .option(ChannelOption.AUTO_READ, false)
                    .connect(new LocalAddress("e4mc-relay"));
            toMinecraft = (LocalChannel) fut.channel();
            fut.addListener(it -> {
                if (it.isSuccess()) {
                    ctx.channel().read();
                } else {
                    QuiclimeSession.this.state = State.UNHEALTHY;
                    if (Agnos.isClient()) {
                        Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                    }
                    ctx.channel().close();
                }
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (toMinecraft.isActive()) {
                toMinecraft.writeAndFlush(msg).addListener(it -> {
                    if (it.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        QuiclimeSession.this.state = State.UNHEALTHY;
                        if (Agnos.isClient()) {
                            Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                        }
                        ((ChannelFuture) it).channel().close();
                    }
                });
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            LOGGER.info("channel inactive(from Quiclime): {} (MC: {})", ctx.channel(), toMinecraft);
            if (toMinecraft.isActive()) {
                toMinecraft.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt.equals(ChannelInputShutdownReadComplete.INSTANCE)) {
                this.channelInactive(ctx);
            }
            super.userEventTriggered(ctx, evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            QuiclimeSession.this.state = State.UNHEALTHY;
            if (Agnos.isClient()) {
                Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
            }
            this.channelInactive(ctx);
        }
    }

    public State state = State.STARTING;
    public enum State {
        STARTING,
        STARTED,
        UNHEALTHY,
        STOPPING,
        STOPPED
    }

    private static class BrokerResponse {
        String id;
        String host;
        int port;
    }

    private final NioEventLoopGroup group = new NioEventLoopGroup();
    private NioDatagramChannel datagramChannel;
    private QuicChannel quicChannel;

    public QuiclimeSession() {
    }

    public void startAsync() {
        var thread = new Thread(this::start, "e4mc_minecraft-init");
        thread.setDaemon(true);
        thread.start();
    }

    private static BrokerResponse getRelay() throws Exception {
        if (Config.INSTANCE.useBroker.value()) {
            var httpClient = HttpClient.newHttpClient();
            var request = HttpRequest
                    .newBuilder(new URI(Config.INSTANCE.brokerUrl.value()))
                    .header("Accept", "application/json")
                    .build();
            LOGGER.info("req: {}", request);
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("resp: {}", response);
            if (response.statusCode() != 200) {
                throw new RuntimeException();
            }
            return gson.fromJson(response.body(), BrokerResponse.class);
        } else {
            var resp = new BrokerResponse();
            resp.id = "custom";
            resp.host = Config.INSTANCE.relayHost.value();
            resp.port = Config.INSTANCE.relayPort.value();
            return resp;
        }
    }

    public void start() {
        try {
            var relayInfo = getRelay();
            LOGGER.info("using relay {}", relayInfo.id);
            QuicSslContext context = QuicSslContextBuilder
                    .forClient()
                    .applicationProtocols("quiclime")
                    .build();
            var codec = new QuicClientCodecBuilder()
                    .sslContext(context)
                    .sslEngineProvider(it -> context.newEngine(it.alloc(), relayInfo.host, relayInfo.port))
                    .initialMaxStreamsBidirectional(512)
                    .maxIdleTimeout(10, TimeUnit.SECONDS)
                    .initialMaxData(4611686018427387903L)
                    .initialMaxStreamDataBidirectionalRemote(1250000)
                    .initialMaxStreamDataBidirectionalLocal(1250000)
                    .initialMaxStreamDataUnidirectional(1250000)
                    .build();
            new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(codec)
                    .bind(0)
                    .addListener(datagramChannelFuture -> {
                if (!datagramChannelFuture.isSuccess()) {
                    QuiclimeSession.this.state = State.UNHEALTHY;
                    if (Agnos.isClient()) {
                        Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                    }
                    throw new RuntimeException(datagramChannelFuture.cause());
                }
                datagramChannel = (NioDatagramChannel) ((ChannelFuture) datagramChannelFuture).channel();
                QuicChannel.newBootstrap(datagramChannel)
                        .streamHandler(
                                new ChannelInitializer<QuicStreamChannel>() {
                                    @Override
                                    protected void initChannel(QuicStreamChannel channel) {
                                        channel.pipeline().addLast(new ToQuiclimeHandler());
                                    }
                                }
                        )
                        .handler(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx, cause);
                                QuiclimeSession.this.state = State.UNHEALTHY;
                                if (Agnos.isClient()) {
                                    Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                                }
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                super.channelInactive(ctx);
                                state = State.STOPPED;
                            }
                        })
                        .streamOption(ChannelOption.AUTO_READ, false)
                        .remoteAddress(new InetSocketAddress(InetAddress.getByName(relayInfo.host), relayInfo.port))
                        .connect()
                        .addListener(quicChannelFuture -> {
                    if (!quicChannelFuture.isSuccess()) {
                        QuiclimeSession.this.state = State.UNHEALTHY;
                        if (Agnos.isClient()) {
                            Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                        }
                        throw new RuntimeException(datagramChannelFuture.cause());
                    }
                    quicChannel = (QuicChannel) quicChannelFuture.get();
                    quicChannel.createStream(QuicStreamType.BIDIRECTIONAL,
                            new ChannelInitializer<QuicStreamChannel>() {
                                @Override
                                protected void initChannel(QuicStreamChannel ch) {
                            ch.pipeline().addLast(new ControlMessageCodec(), new SimpleChannelInboundHandler<ControlMessageCodec.ControlMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ControlMessageCodec.ControlMessage msg) {
                                    if (msg instanceof ControlMessageCodec.DomainAssignmentCompleteMessageClientbound) {
                                        state = State.STARTED;
                                        if (!Agnos.isClient()) {
                                            LOGGER.warn("e4mc running on Dedicated Server; This works, but isn't recommended as e4mc is designed for short-lived LAN servers");
                                        }
                                        String domain = ((ControlMessageCodec.DomainAssignmentCompleteMessageClientbound) msg).domain;
                                        LOGGER.info("Domain assigned: {}", domain);
                                        if (Agnos.isClient()) {
                                            Component message = Mirror.append(Mirror.translatable(
                                                    "text.e4mc_minecraft.domainAssigned",
                                                    Mirror.withStyle(Mirror.literal(domain), it ->
                                                    it
                                                            .withClickEvent(Mirror.copyToClipboard(domain))
                                                            .withColor(ChatFormatting.GREEN)
                                                            .withHoverEvent(Mirror.showText(Mirror.translatable("chat.copy.click"))))
                                            ),
                                                    Mirror.withStyle(Mirror.translatable("text.e4mc_minecraft.clickToStop"), it ->
                                                            it
                                                                    .withClickEvent(Mirror.runCommand("/e4mc stop"))
                                                                    .withColor(ChatFormatting.GRAY)
                                                    )
                                            );
                                            Minecraft.getInstance().gui.getChat().addMessage(message);
                                        }
                                    }
                                    if (msg instanceof ControlMessageCodec.RequestMessageBroadcastMessageClientbound) {

                                        if (Agnos.isClient()) {
                                            Minecraft.getInstance().gui.getChat().addMessage(Mirror.literal(((ControlMessageCodec.RequestMessageBroadcastMessageClientbound) msg).message));
                                        }
                                    }
                                }
                            });
                        }
                    }).addListener(it -> {
                        if (!it.isSuccess()) {
                            QuiclimeSession.this.state = State.UNHEALTHY;
                            if (Agnos.isClient()) {
                                Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
                            }
                            throw new RuntimeException(datagramChannelFuture.cause());
                        }
                        QuicStreamChannel streamChannel = (QuicStreamChannel) it.getNow();
                        LOGGER.info("control channel open: {}", streamChannel);
                        streamChannel
                                .writeAndFlush(new ControlMessageCodec.RequestDomainAssignmentMessageServerbound())
                                .addListener(ignored -> LOGGER.info("control channel write complete"));


                        quicChannel.closeFuture().addListener(ignored -> datagramChannel.close());
                    });
                });
            });
        } catch (Throwable e) {
            QuiclimeSession.this.state = State.UNHEALTHY;
            if (Agnos.isClient()) {
                Minecraft.getInstance().gui.getChat().addMessage(Mirror.translatable("text.e4mc_minecraft.error"));
            }
            throw new RuntimeException(e);
        }
    }

    private static void afterCloseIfPresent(Channel channel, Consumer<Boolean> callback) {
        if (channel == null) {
            callback.accept(false);
        } else {
            channel.close().addListener(it -> callback.accept(true));
        }
    }

    public void stop() {
        state = State.STOPPING;
        afterCloseIfPresent(quicChannel, a -> afterCloseIfPresent(datagramChannel, b -> group.shutdownGracefully().addListener(c -> state = State.STOPPED)));
    }
}
