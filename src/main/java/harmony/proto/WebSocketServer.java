package harmony.proto;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandle;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {

    private final int port;

    public WebSocketServer(int port) {
        this.port = port;
    }

    /**
     * @see <a href="https://github.com/netty/netty/wiki/Netty-4.2-Migration-Guide#new-best-practices">Migration guide</a>
     */
    public void start() throws InterruptedException {
        // EventLoopGroup bossGroup = new NioEventLoopGroup(1); - NioEventLoopGroup is deprecated in Netty 4.2

        // The boss accepts an incoming connection
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        // The worker handles the traffic of the accepted connection
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // Our custom initializer
                    .childHandler(new WebSocketServerInitializer());

            System.out.println("Starting WebSocket Chat Server on port " + port + "...");

            // Bind and start to accept incoming connections
            ChannelFuture f = b.bind("0.0.0.0", port).sync(); // The 0.0.0.0 ip address means that it can accept incoming connections through the internet, not just localhost

            System.out.println("Server started successfully. Waiting for clients...");

            // Wait until the server socket is closed
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 7575; // The port the Client is looking for
        new WebSocketServer(port).start();
    }
}
