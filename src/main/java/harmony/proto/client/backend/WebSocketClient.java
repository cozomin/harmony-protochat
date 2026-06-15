package harmony.proto.client.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import harmony.proto.dto.*;
import harmony.proto.dto.req.*;
import harmony.proto.dto.res.*;
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
//    static final String URL = System.getProperty("url", "ws://127.0.0.1:7575/chat");
    static final int MAX_CONTENT_LENGTH = 8192;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private LiveMessageListener savedMessageListener;
    private LiveGroupCreationListener savedGroupCreationListener;
    private LiveMessageUpdateListener savedMessageUpdateListener;

    private EventLoopGroup group;
    private Channel channel;
    private WebSocketClientHandler handler;
    private List<ChatDTO> chats;

    private String username;

    public WebSocketClient() throws Exception{
//        connect();
    }

    public synchronized void connect() throws Exception {
        if (isConnected()) {
            return;
        }
//        final String URL = System.getProperty("url", "wss://harmony-chat.space/chat");
        final String URL = System.getProperty("url", "ws://127.0.0.1:7575/chat");

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

        if (savedMessageListener != null) {
            handler.setLiveMessageListener(savedMessageListener);
        }

        if  (savedGroupCreationListener != null) {
            handler.setLiveGroupCreationListener(savedGroupCreationListener);
        }

        if (savedMessageUpdateListener != null) {
            handler.setLiveMessageUpdateListener(savedMessageUpdateListener);
        }

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
        channel.writeAndFlush(new TextWebSocketFrame(json));

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
        channel.writeAndFlush(new TextWebSocketFrame(json));

        LoginRes loginRes = handler.awaitLoginResponse();
        if (loginRes.isSuccess()){
            this.username = username;
            return true;
        }
        else return false;
    }

    public synchronized boolean fetchChats() throws Exception {
        handler.prepareForChats();
        String username = handler.getCurrentUsername();
        ChatReq chatReq = new ChatReq(username);
        String json = mapper.writeValueAsString(chatReq);
        channel.writeAndFlush(new TextWebSocketFrame(json));

        ChatRes chatRes = handler.awaitChatResponse();
        if(chatRes.isSuccess()){
            chats = chatRes.getChats();
            return true;
        }
        else return false;
    }

    public synchronized FriendRes friendOperation(FriendOperation operation, String user2) throws Exception{
        //OBS! user2 can be null, depending on the operation. User2 would be taken from a text box
        handler.prepareForFriendOp();
        String user1 = handler.getCurrentUsername();
        FriendReq friendReq = new FriendReq(operation, user1, user2);
        String json = mapper.writeValueAsString(friendReq);
        channel.writeAndFlush(new TextWebSocketFrame(json));

        FriendRes friendRes = handler.awaitFriendOpResponse();

        return friendRes;

    }

    public synchronized List<MessageDTO> fetchMessages(Long chatID) throws Exception {
        handler.prepareForMessages();
        String username = handler.getCurrentUsername();
        MessageReq req = new MessageReq(chatID);

        String json = mapper.writeValueAsString(req);
        channel.writeAndFlush(new TextWebSocketFrame(json));

        MessageRes res = handler.awaitMessageResponse();

        if(res.isSuccess() && res.getChatMessages() != null){
            return res.getChatMessages();
        }

        return new java.util.ArrayList<>();

    }

    public synchronized ChatMembersRes fetchChatMembers(Long chatID) throws Exception {
        handler.prepareForChatMembers();
        ChatMembersReq req = new ChatMembersReq(chatID);
        String json = mapper.writeValueAsString(req);
        channel.writeAndFlush(new TextWebSocketFrame(json));

        return handler.awaitChatMembersResponse();
    }

    public void sendMessage(String input, Long chatID) throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setContent(input);
        messageDTO.setSenderId(handler.getCurrentUsername());
        messageDTO.setChatId(chatID);
        messageDTO.setSentAt(Instant.now());
        messageDTO.setMessageType("regular");

        //Converts Message to JSON for transport
        String jsonMsg = mapper.writeValueAsString(messageDTO);
//        System.out.println(jsonMsg);

        //Sends the json through a TextWebSocketFrame
        WebSocketFrame frame = new TextWebSocketFrame(jsonMsg);
        channel.writeAndFlush(frame);
    }

    public void createGroup(String groupName, List<String> topics, List<String> members) throws Exception {
        GroupCreationReq groupCreationReq = new GroupCreationReq();
        groupCreationReq.setName(groupName);
        groupCreationReq.setTopics(topics);
        groupCreationReq.setCreator(username);
        groupCreationReq.setMembers(members);

        String json = mapper.writeValueAsString(groupCreationReq);
        channel.writeAndFlush(new TextWebSocketFrame(json));
    }

    public void editMessage(Long messId, Long chatId, String newContent) throws Exception {
        MessageEditReq req = new MessageEditReq(messId, chatId, newContent);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));
    }

    public void deleteMessage(Long messId, Long chatId) throws Exception {
        MessageDeleteReq req = new MessageDeleteReq(messId, chatId);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));
    }

    public synchronized List<String> fetchInterests() throws Exception {
        handler.prepareForInterests();
        InterestsReq req = new InterestsReq(InterestsOperation.FETCH.name(), handler.getCurrentUsername(), null);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        InterestsRes res = handler.awaitInterestsResponse();
        if (res.isSuccess() && res.getInterests() != null) {
            return res.getInterests();
        }
        return new java.util.ArrayList<>();
    }

    public synchronized List<String> fetchTopPopularInterests() throws Exception {
        handler.prepareForInterests();

        // Pass null for username and interest since this is a global query
        InterestsReq req = new InterestsReq(InterestsOperation.FETCH_TOP.name(), null, null);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        InterestsRes res = handler.awaitInterestsResponse();
        if (res.isSuccess() && res.getInterests() != null) {
            return res.getInterests();
        }

        return new java.util.ArrayList<>();
    }

    public synchronized List<String> addInterest(String interest) throws Exception {
        handler.prepareForInterests();
        InterestsReq req = new InterestsReq(InterestsOperation.ADD.name(), handler.getCurrentUsername(), interest);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        InterestsRes res = handler.awaitInterestsResponse();
        if (res.isSuccess() && res.getInterests() != null) {
            return res.getInterests();
        }
        return new java.util.ArrayList<>();
    }

    public synchronized List<String> removeInterest(String interest) throws Exception {
        handler.prepareForInterests();

        InterestsReq req = new InterestsReq(InterestsOperation.REMOVE.name(), handler.getCurrentUsername(), interest);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        InterestsRes res = handler.awaitInterestsResponse();
        if (res.isSuccess() && res.getInterests() != null) {
            return res.getInterests();
        }
        return new java.util.ArrayList<>();
    }

    public synchronized String polishMessageText(String originalText) throws Exception {
        handler.prepareForAIPolish();
        AIPolishReq req = new AIPolishReq(originalText);
        String json = mapper.writeValueAsString(req);
        channel.writeAndFlush(new TextWebSocketFrame(json));

        AIPolishRes res = handler.awaitAIPolishResponse();
        if ("success".equals(res.getMessage()) && res.getPolishedText() != null) {
            return res.getPolishedText();
        }
        return originalText;
    }

    public synchronized List<String> getAIRecommendedGroups(String topic) throws Exception {
        handler.prepareForAIGroupReq();
        AIRecommendGroupsReq req = new AIRecommendGroupsReq(topic);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        AIRecommendGroupsRes res = handler.awaitAIGroupResponse();
        if (res.isSuccess() && res.getRecommendedGroups() != null) {
            return res.getRecommendedGroups();
        }
        return new java.util.ArrayList<>();
    }

    public synchronized boolean joinGroup(String groupName) throws Exception {
        handler.prepareForJoinGroup();
        JoinGroupReq req = new JoinGroupReq(groupName);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        JoinGroupRes res = handler.awaitJoinGroupResponse();
        return res != null && res.isSuccess();
    }

    public LeaveGroupRes leaveGroup(String groupName) throws Exception {
        handler.prepareForLeaveGroup();
        LeaveGroupReq req = new LeaveGroupReq(groupName);
        channel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(req)));
        LeaveGroupRes res = handler.awaitLeaveGroupResponse();
        return res;
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
        this.savedMessageListener = listener;
        if (handler != null) {
            handler.setLiveMessageListener(listener);
        }
    }

    public void setLiveGroupCreationListener(LiveGroupCreationListener listener) {
        this.savedGroupCreationListener = listener;
        if (handler != null) {
            handler.setLiveGroupCreationListener(listener);
        }
    }
    public void setLiveMessageUpdateListener(LiveMessageUpdateListener listener) {
        this.savedMessageUpdateListener = listener;
        if (handler != null) {
            handler.setLiveMessageUpdateListener(listener);
        }
    }
}