package harmony.proto.dao;

import harmony.proto.dto.MessageDTO;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageDaoTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private MessageDao messageDao;

    @BeforeEach
    public void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testSaveMessage_WithTransaction() throws SQLException {
        MessageDTO msg = new MessageDTO("sender1", 10L, "Hello", Instant.now(), "regular");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("messID")).thenReturn(99L);

        // Act
        messageDao.save(msg);

        // Assert
        assertEquals(99L, msg.getMessId(), "Message ID should be updated from DB");
        verify(connection).setAutoCommit(false);
        verify(preparedStatement).executeUpdate();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    public void testSaveMessage_RollbackOnError() throws SQLException {
        MessageDTO msg = new MessageDTO("sender1", 10L, "Hello", Instant.now(), "regular");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB Crash"));

        // Act and Assert
        assertThrows(SQLException.class, () -> messageDao.save(msg));

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }

    @Test
    public void testFindRecentMessages() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("messID")).thenReturn(1L);
        when(resultSet.getString("senderID")).thenReturn("user1");
        when(resultSet.getLong("chatID")).thenReturn(10L);
        when(resultSet.getString("message_content")).thenReturn("Test message");
        when(resultSet.getTimestamp("sent_at")).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getString("message_type")).thenReturn("regular");

        List<MessageDTO> result = messageDao.findRecentMessages(10L, 50);

        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getContent());
        verify(preparedStatement).setInt(2, 50); // limit parameter
    }

    @Test
    public void testDeleteMessage() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        messageDao.deleteMessage(105L);

        verify(preparedStatement).setLong(1, 105L);
        verify(preparedStatement).executeUpdate();
    }
}