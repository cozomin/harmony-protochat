package harmony.proto.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariDataSource;
import harmony.proto.database.connection_manager;
import harmony.proto.dto.MessageDTO;
import harmony.proto.dto.req.*;
import harmony.proto.dto.res.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebSocketServerHandlerTest {

    private WebSocketServerHandler handler;
    private EmbeddedChannel channel;
    private ObjectMapper mapper;

    private HikariDataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setup() throws Exception {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = new WebSocketServerHandler();
        channel = new EmbeddedChannel(handler);

        // JDBC MOCK
        mockDataSource = mock(HikariDataSource.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Base behaviour
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Injection of the data source mock
        injectGlobalDataSource(mockDataSource);
    }

    private void injectGlobalDataSource(HikariDataSource mockDataSource) {
        try {

            for (Field field : connection_manager.class.getDeclaredFields()) {

                if (HikariDataSource.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    field.set(null, mockDataSource);
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock DB: " + e.getMessage(), e);
        }
    }

    @Test
    public void testProcessLoginReq_Success() throws Exception {
        LoginReq req = new LoginReq("cos", "pass123");

        String validHash = BCrypt.hashpw("pass123", BCrypt.gensalt());
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn(validHash);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        Thread.sleep(300);
        channel.runPendingTasks();

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response, "The server must respond");

        LoginRes res = mapper.readValue(response.text(), LoginRes.class);
        assertTrue(res.isSuccess(), "The login should have been successful");
        assertEquals("cos", channel.attr(WebSocketServerHandler.usernameKEY).get());
    }

    @Test
    public void testProcessMessageReq_UnauthorizedBlocked() throws Exception {
        MessageReq req = new MessageReq(10L);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(req)));
        Thread.sleep(300);
        channel.runPendingTasks();

        TextWebSocketFrame response = channel.readOutbound();
        assertNull(response, "An unauthorized user should not receive a response");
    }

    @Test
    public void testProcessChatReq_Authorized() throws Exception {
        channel.attr(WebSocketServerHandler.usernameKEY).set("cos");
        ChatReq req = new ChatReq();

        when(mockResultSet.next()).thenReturn(false);

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(req)));
        Thread.sleep(300);
        channel.runPendingTasks();

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response, "The response must not be null after running pending tasks");
        ChatRes res = mapper.readValue(response.text(), ChatRes.class);
        assertTrue(res.isSuccess());
    }

    @Test
    public void testGroupCreationReq_BroadcastsToCreator() throws Exception {
        channel.attr(WebSocketServerHandler.usernameKEY).set("creator");

        // Ensure the user's presence in onlineUsers
        try {
            Field onlineUsersField = WebSocketServerHandler.class.getDeclaredField("onlineUsers");
            onlineUsersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, io.netty.channel.Channel> map =
                    (java.util.Map<String, io.netty.channel.Channel>) onlineUsersField.get(null);
            map.put("creator", channel);
        } catch (Exception e) {
            // Ignore if the field does not exist/has been renamed
        }

        GroupCreationReq req = new GroupCreationReq("My Group", Arrays.asList("Tech"), "desc", Arrays.asList("link"), "creator");

        when(mockResultSet.next()).thenReturn(true, true, true, true, false);

        // Provide valid data for anything the DAO might request from the database to avoid NullPointerException
        when(mockResultSet.getLong("chatID")).thenReturn(99L);
        when(mockResultSet.getLong("topicID")).thenReturn(42L);
        when(mockResultSet.getString(anyString())).thenReturn("creator"); // Any text column will return "creator"
        when(mockResultSet.getString(anyInt())).thenReturn("creator");

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(req)));

        // Wait for the DB Executor to finish all queries
        Thread.sleep(400);
        channel.runPendingTasks();

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response, "The server must confirm the group creation. If it returns null, check the console for an exception in the handler!");

        GroupCreationRes res = mapper.readValue(response.text(), GroupCreationRes.class);
        assertEquals("creator", res.getCreator(), "The response must contain the creator's name");
        assertTrue(res.isSuccess() || res.getMessage().contains("success"), "The message must be successful");
    }

    @Test
    public void testSaveMessageAndBroadcast() throws Exception {
        channel.attr(WebSocketServerHandler.usernameKEY).set("cos");
        MessageDTO incomingMsg = new MessageDTO("cos", 10L, "Hello Group!", null, "regular");

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("messID")).thenReturn(105L);
        when(mockResultSet.getString("username")).thenReturn("cos", "link");

        channel.writeInbound(new TextWebSocketFrame(mapper.writeValueAsString(incomingMsg)));
        Thread.sleep(300);
        channel.runPendingTasks();

        // Verify if the DAO executed the INSERT
        verify(mockPreparedStatement, atLeastOnce()).setString(1, "cos");
        verify(mockPreparedStatement, atLeastOnce()).setString(3, "Hello Group!");
    }
}