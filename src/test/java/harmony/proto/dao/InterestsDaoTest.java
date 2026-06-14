package harmony.proto.dao;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestsDaoTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private InterestsDao interestsDao;

    @BeforeEach
    public void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testFetchUserInterests() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("Cybersecurity");

        List<String> interests = interestsDao.fetchUserInterests("user123");

        assertEquals(1, interests.size());
        assertEquals("Cybersecurity", interests.get(0));
    }

    @Test
    public void testAddInterest_WithTransaction() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // Mock
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("topicID")).thenReturn(42L);

        // Act
        interestsDao.addInterest("john_doe", "Java");

        // Assert
        verify(connection).setAutoCommit(false);

        verify(preparedStatement, atLeastOnce()).executeUpdate();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    public void testRemoveInterest() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        interestsDao.removeInterest("john_doe", "Java");

        verify(preparedStatement).setString(1, "john_doe");
        verify(preparedStatement).setString(2, "Java");
        verify(preparedStatement).executeUpdate();
    }
}