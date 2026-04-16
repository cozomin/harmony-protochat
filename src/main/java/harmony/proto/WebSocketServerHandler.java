package harmony.proto;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // ChannelGroup holds all active connections so we can broadcast messages
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE); // just for 1 group chat
    // 1 Channel = 1 Connection to a Client => ChannelGroup = multiple connections to different clients
    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>(); // a collection of group chats

    // When someone is added to the server
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // Broadcast to everyone else that someone joined
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has joined!\n"));
        channels.add(incoming);
    }

    // When someone leaves the server
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // Broadcast that someone left
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " has left!\n"));
        channels.remove(incoming);
    }

    // Handles only text right now. It could handle binary messages or other frame types

    /**
     * @see <a href="https://www.w3computing.com/articles/building-high-performance-websocket-applications-java-netty/#:~:text=Advanced%20WebSocket%20Features%3A-,Binary%20and%20text%20messages">More explanations</a>
     */

    //TODO: Handling WebSocket message fragmentation and continuation
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception{ // Second parameter can be WebSocketFrame and then switch/if elses to verify the data type
        Channel incoming = ctx.channel(); // The specific connection to a client
        String messageText = msg.text(); // Client message

        // Broadcast the message to all clients
        for (Channel channel : channels) {
            if (channel != incoming) {
                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "] " + messageText));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame("[you] " + messageText));
            }
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
