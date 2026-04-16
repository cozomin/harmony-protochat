package harmony.proto;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // 1. Handle HTTP requests
        pipeline.addLast(new HttpServerCodec());
        // 2. Aggregate HTTP requests into FullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 3. Handle WebSocket handshake and control frames (Ping/Pong/Close) on the "/chat" endpoint
        pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));
        // 4. We trigger an event if no traffic flows in either direction for 50 seconds.
        pipeline.addLast(new IdleStateHandler(0, 0, 50, TimeUnit.SECONDS));
        // 5. Our custom handler for chat messages
        pipeline.addLast(new WebSocketServerHandler());
    }
}