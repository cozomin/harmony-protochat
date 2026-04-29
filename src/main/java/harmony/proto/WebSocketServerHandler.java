package harmony.proto;

import harmony.proto.database.Message;
import harmony.proto.database.MessageDao;
import harmony.proto.database.connection_manager;
import harmony.proto.database.db_config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Message> {
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
        // Broadcast to everyone else that someone joined
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has joined!\n"));
        channels.add(incoming);

        Message systemMsg = new Message();
        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " joined");
        systemMsg.setMessageType("SYSTEM");

        try {
            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // When someone leaves the server
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // Broadcast that someone left
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has left!\n"));
        channels.remove(incoming);
        Message systemMsg = new Message();
        systemMsg.setContent("[SERVER] - " + incoming.remoteAddress() + " has left");
        systemMsg.setMessageType("SYSTEM");

        try {
            channels.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(systemMsg)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Handles only text right now. It could handle binary messages or other frame types

    /**
     * @see <a href="https://www.w3computing.com/articles/building-high-performance-websocket-applications-java-netty/#:~:text=Advanced%20WebSocket%20Features%3A-,Binary%20and%20text%20messages">More explanations</a>
     */

    //TODO: Handling WebSocket message fragmentation and continuation
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception{
//        // Second parameter can be WebSocketFrame and then switch/if elses to verify the data type
//
//        Channel incoming = ctx.channel(); // The specific connection to a client
//        String messageText = msg.text() ; // Client message
//
//        // Broadcast the message to all clients
//        for (Channel channel : channels) {
//            if (channel != incoming) {
//                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "] " + messageText));
//            } else {
//                channel.writeAndFlush(new TextWebSocketFrame("[you] " + messageText));
//            }
//        }
//        //resource release not necessary, using SimpleHandler
//        //NOTE: shouldn’t store references to any messages for later use, as these will become invalid.
//    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception{
        // Second parameter can be WebSocketFrame and then switch/if elses to verify the data type

        Channel incoming = ctx.channel(); // The specific connection to a client
        String messageText = msg.getContent() ; // Client message

        if (messageText == null || messageText.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            saveToDatabase(msg);
        }, dbExecutor).thenRun(() -> {
            // After storing in database, we send a confirmation on the I/O thread
            Message confirm = new Message();
            confirm.setContent("Mesaj salvat!");
            confirm.setMessageType("sistem");
            confirm.setSentAt(Instant.now());

            try {
                String json = mapper.writeValueAsString(confirm);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        String jsonMessage = mapper.writeValueAsString(msg);

        // Broadcast the message to all clients
        for (Channel channel : channels) {
//            if (channel != incoming) {
//                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "] " + messageText));
//            } else {
//                channel.writeAndFlush(new TextWebSocketFrame("[you] " + messageText));
//            }
            channel.writeAndFlush(new TextWebSocketFrame(jsonMessage));
        }
        //resource release not necessary, using SimpleHandler
        //NOTE: shouldn’t store references to any messages for later use, as these will become invalid.
    }

    //TODO Move the connection logic in main
    private void saveToDatabase(Message msg) {
        db_config db_conf = new db_config("jdbc:postgresql://localhost:5432/harmony", "postgres",
                "SQLpa55", 4);

        connection_manager.init(db_conf);
        DataSource ds = connection_manager.getDataSource();
        MessageDao messageDao = new MessageDao(ds);
        try (Connection conn = ds.getConnection();) {
            messageDao.save(msg);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if (evt instanceof IdleStateEvent){
            // The connection has been idle for 50 seconds
            // Send a PingWebSocketFrame to keep the Cloudflare tunnel alive
            System.out.println("Connection idle, sending Ping...");
            ctx.writeAndFlush(new PingWebSocketFrame());
        }
        else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

