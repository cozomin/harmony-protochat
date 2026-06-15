package harmony.proto.dao;

import harmony.proto.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDaoTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private UserDao userDao;

    @BeforeEach
    public void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testExistsByUsername() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(userDao.existsByUsername("testUser"));
        verify(preparedStatement).setString(1, "testUser");
    }

    @Test
    public void testLogin_Success() throws SQLException {
        String plainPassword = "password123";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn(hashedPassword);

        String result = userDao.login("testUser", plainPassword);
        assertEquals("testUser", result);
    }

    @Test
    public void testSignUp_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1); // 1 row affected

        String result = userDao.signUp("newUser", "hashedPass");
        assertEquals("newUser", result);
        verify(preparedStatement).setString(1, "newUser");
    }

    @Test
    public void testAddDM_WithTransaction() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("chatID")).thenReturn(50L);

        // Act
        userDao.addDM("user1", "user2");

        // Assert Transaction Handling
        verify(connection).setAutoCommit(false);
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement).executeBatch();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    public void testFetchFriends() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("friend")).thenReturn("friend1", "friend2");

        List<UserDTO> friends = userDao.fetchFriends("user1", "accepted");

        assertEquals(2, friends.size());
        assertEquals("friend1", friends.get(0).getUsername());
        assertEquals("friend2", friends.get(1).getUsername());
    }
}