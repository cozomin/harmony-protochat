package harmony.proto.server;

import harmony.proto.dao.ChatDao;
import harmony.proto.dao.UserDao;
import harmony.proto.dto.*;
import harmony.proto.dao.MessageDao;
import harmony.proto.database.connection_manager;
import harmony.proto.database.db_config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.sql.DataSource;
import java.io.DataInput;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // ChannelGroup holds all active connections so we can broadcast messages
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE); // just for 1 group chat
    // 1 Channel = 1 Connection to a Client => ChannelGroup = multiple connections to different clients
    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>(); // a collection of group chats
    //to be used for multiple groups or something idk

    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(20);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // When someone is added to the server
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
//        // Broadcast to everyone else that someone joined
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has joined!\n"));
//        channels.add(incoming);
//
//        Message systemMsg = new Message();
//        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " joined");
//        systemMsg.setMessageType("SYSTEM");
//
//        try {
//            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
//        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("handler added " + incoming.remoteAddress());
    }

    // When someone leaves the server
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // Broadcast that someone left
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has left!\n"));
//        channels.remove(incoming);
//        Message systemMsg = new Message();
//        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " has left");
//        systemMsg.setMessageType("SYSTEM");
//
//        try {
//            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
//        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("handler removed " + incoming.remoteAddress());

    }

    // Handles only text right now. It could handle binary messages or other frame types

    /**
     * @see <a href="https://www.w3computing.com/articles/building-high-performance-websocket-applications-java-netty/#:~:text=Advanced%20WebSocket%20Features%3A-,Binary%20and%20text%20messages">More explanations</a>
     */

    //TODO: Handling WebSocket message fragmentation and continuation
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // Second parameter can be WebSocketFrame and then switch/if elses to verify the data type

        String json = msg.text();
        BaseDTO dto = mapper.readValue(json, BaseDTO.class);

        if (dto instanceof MessageDTO messageDTO) {
            String messageText = messageDTO.getContent(); // Client message

            if (messageText == null || messageText.isEmpty()) {
                return;
            }

            //save to database on a different thread
//            CompletableFuture.runAsync(() -> {
//                saveToDatabase(messageDTO);
//            }, dbExecutor);

            //Pass it to the db Handler to save the message in the database
            ctx.fireChannelRead(messageDTO);

            //.thenRun(() -> {
//                // After storing in database, we send a confirmation on the I/O thread
//                Message confirm = new Message();
//                confirm.setContent("Mesaj salvat!");
//                confirm.setMessageType("sistem");
//                confirm.setSentAt(Instant.now());
//
//                try {
//                    String json = mapper.writeValueAsString(confirm);
//                    ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).exceptionally(ex -> {
//                ex.printStackTrace();
//                return null;
//            });
        } else if (dto instanceof LoginReq login) {
            LoginRes res = processLoginReq(login);
            String jsonRes = mapper.writeValueAsString(res);
            TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
            ctx.channel().writeAndFlush(frame);
        }
        else if(dto instanceof ChatReq req){
            ChatRes res = processChatReq(req);
            String jsonRes = mapper.writeValueAsString(res);
            TextWebSocketFrame frame = new TextWebSocketFrame(jsonRes);
            ctx.channel().writeAndFlush(frame);
        }


        // Notify that chat has been updated?
    }

    private void saveToDatabase(MessageDTO msg) {
        DataSource ds = connection_manager.getDataSource();
        MessageDao messageDao = new MessageDao(ds);

        try (Connection conn = ds.getConnection();) {
            messageDao.save(msg);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatRes processChatReq(ChatReq req){
        DataSource ds = connection_manager.getDataSource();
        ChatDao dao = new ChatDao(ds);
        List<ChatDTO> chats = new ArrayList<>();

        try{
            chats = dao.findUserChats(req.getUserID());
        } catch (SQLException e){
            return new ChatRes("Database error" +  e.getMessage(),
                    0L, null);
        }
        return new ChatRes("success", (long) chats.size(), chats);

    }

    private LoginRes processLoginReq(LoginReq req) {

        DataSource ds = connection_manager.getDataSource();
        UserDao userDao = new UserDao(ds);

        try {
            if (!userDao.existsByUsername(req.getUsername())) {
                return new LoginRes("Username does not exist", null);
            }
            Long userID = userDao.login(req.getUsername(), req.getPassword());
            if(userID == null || userID == -1) {
                return new LoginRes("Wrong password!", null);
            }
            return new LoginRes("success", userID);

        } catch (SQLException e) {
            return new LoginRes("Database failure: " + e.getMessage(), null);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // The connection has been idle for 50 seconds
            // Send a PingWebSocketFrame to keep the Cloudflare tunnel alive
            System.out.println("Connection idle, sending Ping...");
            ctx.writeAndFlush(new PingWebSocketFrame());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}