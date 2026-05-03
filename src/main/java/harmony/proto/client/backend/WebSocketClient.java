package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.*;
import harmony.proto.dto.req.ChatReq;
import harmony.proto.dto.req.LoginReq;
import harmony.proto.dto.req.MessageReq;
import harmony.proto.dto.req.SignUpReq;
import harmony.proto.dto.res.ChatRes;
import harmony.proto.dto.res.LoginRes;
import harmony.proto.dto.res.MessageRes;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public final class WebSocketClient {

//    static final String URL = System.getProperty("url", "wss://harmony-chat.space/chat");
    static final String URL = System.getProperty("url", "ws://127.0.0.1:7575/chat");
    static final int MAX_CONTENT_LENGTH = 8192;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private EventLoopGroup group;
    private Channel channel;
    private WebSocketClientHandler handler;
    private List<ChatDTO> chats;

    private String username;

    public WebSocketClient() throws Exception{
        connect();
    }

    public synchronized void connect() throws Exception {
        if (isConnected()) {
            return;
        }

        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;

        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                throw new IllegalStateException("Unsupported scheme: " + scheme);
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException("Only WS(S) is supported.");
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx = ssl
                ? SslContextBuilder.forClient()
                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
                  .build()
                : null;

        group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        handler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
                )
        );

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        p.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                                new WebSocketClientCompressionHandler(MAX_CONTENT_LENGTH),
                                handler
                        );
                    }
                });

        channel = b.connect(host, port).sync().channel();
        handler.handshakeFuture().sync();
    }

    public boolean login(String username, String password) throws Exception {
        connect();
        handler.prepareForLogin();

        LoginReq loginReq = new LoginReq(username, password);
        String json = mapper.writeValueAsString(loginReq);
        channel.writeAndFlush(new TextWebSocketFrame(json)).sync();

        LoginRes loginRes = handler.awaitLoginResponse();
        if (loginRes.isSuccess()){
            this.username = username;
            return true;
        }
        else return false;
    }

    public boolean signUp(String username, String password) throws Exception{
        connect();
        handler.prepareForLogin();

        SignUpReq signUpReq = new SignUpReq(username, password);
        String json = mapper.writeValueAsString(signUpReq);
        channel.writeAndFlush(new TextWebSocketFrame(json)).sync();

        LoginRes loginRes = handler.awaitLoginResponse();
        if (loginRes.isSuccess()){
            this.username = username;
            return true;
        }
        else return false;
    }

    public boolean fetchChats() throws Exception {
        handler.prepareForChats();
        Long userID = handler.getCurrentUserId();
        ChatReq chatReq = new ChatReq(userID);
        String json = mapper.writeValueAsString(chatReq);
        channel.writeAndFlush(new TextWebSocketFrame(json)).sync();

        ChatRes chatRes = handler.awaitChatResponse();
        if(chatRes.isSuccess()){
            chats = chatRes.getChats();
            return true;
        }
        else return false;
    }

    public List<MessageDTO> fetchMessages(Long chatID) throws Exception {
        handler.prepareForMessages();
        Long userID = handler.getCurrentUserId();
        MessageReq req = new MessageReq(userID, chatID);

        String json = mapper.writeValueAsString(req);
        channel.writeAndFlush(new TextWebSocketFrame(json)).sync();

        MessageRes res = handler.awaitMessageResponse();

        if(res.isSuccess()){
            return res.getChatMessages();
        }
        return null;

    }

    public void sendMessage(String input, Long chatID) throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setContent(input);
        messageDTO.setSenderId(handler.getCurrentUserId());
//        messageDTO.setChatId(1L);   // test chatID
        messageDTO.setChatId(chatID);
        messageDTO.setSentAt(Instant.now());
        messageDTO.setMessageType("regular");

        //Converts Message to JSON for transport
        String jsonMsg = mapper.writeValueAsString(messageDTO);

        //Sends the json through a TextWebSocketFrame
        WebSocketFrame frame = new TextWebSocketFrame(jsonMsg);
        channel.writeAndFlush(frame);
    }

    public synchronized void disconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new CloseWebSocketFrame()).sync();
            channel.close().syncUninterruptibly();
        }

        if (group != null) {
            group.shutdownGracefully().syncUninterruptibly();
        }

        channel = null;
        group = null;
        handler = null;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public Long getCurrentUserId() {
        return handler != null ? handler.getCurrentUserId() : null;
    }

    public String getCurrentUsername() {
        return handler != null ? username : null;
    }

    public String getLoginFailureReason() {
        return handler != null ? handler.getLoginFailureReason() : null;
    }

    public List<ChatDTO> getChats() {
        return chats;
    }

    public void setLiveMessageListener(LiveMessageListener listener) {
        if (handler != null) {
            handler.setLiveMessageListener(listener);
        }
    }
}