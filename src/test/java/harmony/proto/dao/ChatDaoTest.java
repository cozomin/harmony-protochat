package harmony.proto.dao;

import harmony.proto.dto.ChatDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatDaoTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ChatDao chatDao;

    @BeforeEach
    public void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testIsMember() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(chatDao.isMember(10L, "user1"));
        verify(preparedStatement).setLong(1, 10L);
        verify(preparedStatement).setString(2, "user1");
    }

    @Test
    public void testAddGroup_WithTransaction() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Mock
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("chatID")).thenReturn(100L);

        List<String> topics = Arrays.asList("Gaming", "Tech");
        List<String> members = Arrays.asList("user1", "user2");

        // Act
        chatDao.addGroup("Super Group", topics, "adminUser", members);

        // Assert
        verify(connection).setAutoCommit(false);
        verify(preparedStatement, atLeastOnce()).addBatch();
        verify(preparedStatement, atLeastOnce()).executeBatch();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    public void testFindTopicsForChat() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("Programming", "Networking");

        List<String> topics = chatDao.findTopicsForChat(15L);

        assertEquals(2, topics.size());
        assertTrue(topics.contains("Programming"));
        assertTrue(topics.contains("Networking"));
    }

    @Test
    public void testJoinGroupByName() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        chatDao.joinGroupByName("Global Tech", "newUser");

        verify(preparedStatement).setString(1, "newUser");
        verify(preparedStatement).setString(2, "Global Tech");
        verify(preparedStatement).executeUpdate();
    }
}