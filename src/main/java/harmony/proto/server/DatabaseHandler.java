package harmony.proto.server;

import harmony.proto.dto.MessageDTO;
import harmony.proto.dao.MessageDao;
import harmony.proto.database.connection_manager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// This handler only processes MessageDTO objects
public class DatabaseHandler extends SimpleChannelInboundHandler<MessageDTO> {

    // Create a standard Java thread pool
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(20);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageDTO msg) {
        dbExecutor.submit(() -> {
            DataSource ds = connection_manager.getDataSource();
            MessageDao messageDao = new MessageDao(ds);

            try (Connection conn = ds.getConnection()) {
                messageDao.save(msg);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}