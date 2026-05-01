package harmony.proto.server;
import harmony.proto.database.*;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.sql.SQLException;

public class WebSocketServer {

    private final int port;

    public WebSocketServer(int port) {
        this.port = port;
    }

    /**
     * @see <a href="https://github.com/netty/netty/wiki/Netty-4.2-Migration-Guide#new-best-practices">Migration guide</a>
     */
    public void start() throws InterruptedException, SQLException {
//        db_config db_conf = new db_config("jdbc:postgresql://localhost:5432/harmony", "postgres",
//                    "SQLpa55", 4);
//
//        connection_manager.init(db_conf);
//
//        DataSource ds = connection_manager.getDataSource();
//        try {
//            PreparedStatement statement =  ds.getConnection().prepareStatement("select * from hm_user");
//            ResultSet rs = statement.executeQuery();
//            while(rs.next()){
//                System.out.println(rs.getString("username") + ' ' + rs.getString("pass"));
//            }
//        }catch (SQLException e){
//            System.out.println("Caught SQLexception" + e.getMessage());
//        }
//        UserDao userDao = new UserDao(ds);
//        System.out.println(userDao.existsById(4L));
//        ChatDao chatDao = new ChatDao(ds);
//        List<Chat> chats = new ArrayList<Chat>();
//        chats = chatDao.findUserChats(1);
//        System.out.println(chats.get(0).getChatName());

        db_config db_conf = new db_config("jdbc:postgresql://localhost:5432/harmony", "postgres", "SQLpa55", 4);
        connection_manager.init(db_conf);
        System.out.println("Connceted to DB!\n");


        // EventLoopGroup bossGroup = new NioEventLoopGroup(1); - NioEventLoopGroup is deprecated in Netty 4.2

        // The boss accepts an incoming connection
        final EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        // The worker handles the traffic of the accepted connection
        final EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        // The database executor manages the connection in a separate thread group from the rest
//        final EventExecutorGroup dbGroup = new DefaultEventExecutorGroup(16);

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
            connection_manager.shutdown();
        }
    }

    public static void main(String[] args) throws InterruptedException, SQLException {
        int port = 7575; // The port the Client is looking for
        new WebSocketServer(port).start();
    }
}