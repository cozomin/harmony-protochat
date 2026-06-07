package harmony.proto.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InterestsDao {
    private final DataSource dataSource;

    public InterestsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> fetchUserInterests(String username) throws SQLException {
        List<String> interests = new ArrayList<>();

        String query = "SELECT t.name FROM hm_topics t JOIN user_topic ut ON t.topicID = ut.topicID WHERE ut.userID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interests.add(rs.getString("name"));
                }
            }
        }
        return interests;
    }

    public List<String> fetchTopInterests(int limit) throws SQLException {
        List<String> topInterests = new ArrayList<>();

        String query = """
            SELECT t.name
            FROM hm_topics t
            LEFT JOIN user_topic ut ON t.topicID = ut.topicID
            GROUP BY t.topicID, t.name
            ORDER BY COUNT(ut.userID) DESC
            LIMIT ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topInterests.add(rs.getString("name"));
                }
            }
        }
        return topInterests;
    }

    public void addInterest(String username, String interest) throws SQLException {
        String insertTopic = "INSERT INTO hm_topics (name, is_official) VALUES (?, false) ON CONFLICT (name) DO NOTHING";
        String getTopicId = "SELECT topicID FROM hm_topics WHERE name = ?";
        String linkUser = "INSERT INTO user_topic (userID, topicID) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(insertTopic)) {
                stmt1.setString(1, interest);
                stmt1.executeUpdate();
            }

            long topicId = -1;
            try (PreparedStatement stmt2 = conn.prepareStatement(getTopicId)) {
                stmt2.setString(1, interest);
                try (ResultSet rs = stmt2.executeQuery()) {
                    if (rs.next()) {
                        topicId = rs.getLong("topicID");
                    }
                }
            }

            if (topicId != -1) {
                try (PreparedStatement stmt3 = conn.prepareStatement(linkUser)) {
                    stmt3.setString(1, username);
                    stmt3.setLong(2, topicId);
                    stmt3.executeUpdate();
                }
            }
        }
    }

    public void removeInterest(String username, String interest) throws SQLException {
        String query = "DELETE FROM user_topic WHERE userID = ? AND topicID = (SELECT topicID FROM hm_topics WHERE name = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, interest);
            stmt.executeUpdate();
        }
    }
}