package harmony.proto.dao;

import harmony.proto.dto.ChatDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ChatDao {
    private final DataSource dataSource;

    public ChatDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isMember(Long chatId, String username) throws SQLException {
        String sql = "select 1 from chat_member where chatID = ? and memberID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

//TODO: every try() block is useless

    public List<String> findUsersInChat(long chatID) throws SQLException{
        String sql = "select cm.memberid \n" +
                "from chat_member cm \n" +
                "where cm.chatID = ?";
        List<String> users = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    users.add(rs.getString("memberid"));
                }
            }
        }
        return users;
    }

    public List<ChatDTO> findUserChats(String username) throws SQLException {
        //selects all chats that userID is a member of
        String groupSql = "select chatID, chatName, isGroup, updated_at\n" +
                "from chat join chat_member using (chatID)\n" +
                "where memberID = ? and isgroup = true";

        String dmSql = "select c.chatID, c.isGroup, c.updated_at, cm_other.memberid as dmName\n" +
                "from chat c join chat_member cm on (c.chatid = cm.chatid) join chat_member cm_other on (c.chatid = cm_other.chatid)\n" +
                "where cm.memberID = ? and c.isgroup = false and cm_other.memberid != ?;";

        List<ChatDTO> chatDTOS = new ArrayList<>();

        try (Connection con = dataSource.getConnection();) {
            try (PreparedStatement psGroup = con.prepareStatement(groupSql)) {
                psGroup.setString(1, username);
                try (ResultSet rs = psGroup.executeQuery()) {
                    while (rs.next()) {
                        ChatDTO chatDTO = new ChatDTO();
                        chatDTO.setChatID(rs.getLong("chatID"));
                        chatDTO.setGroup(rs.getBoolean("isGroup"));
                        //                    chat.setUpdated_at(rs.getTimestamp("updated_at").toInstant());
                        //updated_at is not yet implemented, if null will crash server
                        chatDTO.setChatName(rs.getString("chatName"));

                        chatDTOS.add(chatDTO);
                    }
                }
            }

            try (PreparedStatement psDm = con.prepareStatement(dmSql);) {
                psDm.setString(1, username);
                psDm.setString(2, username);

                try (ResultSet rs = psDm.executeQuery()) {
                    while (rs.next()) {
                        ChatDTO chatDTO = new ChatDTO();
                        chatDTO.setChatID(rs.getLong("chatID"));
                        chatDTO.setGroup(rs.getBoolean("isGroup"));
//                    chat.setUpdated_at(rs.getTimestamp("updated_at").toInstant());
                        //updated_at is not yet implemented, if null will crash server
                        chatDTO.setChatName(rs.getString("dmName"));
                        chatDTOS.add(chatDTO);
                    }
                }
            }

            return chatDTOS;
        }
    }

    private String findDmName(String username, long chatID) throws SQLException{
        String sql = """
                select hm_user.username\s
                from chat_member join hm_user on (hm_user.username = chat_member.memberID)\s
                where chat_member.chatID = ? and hm_user.username != ?""";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getString("username");
                else
                    return "Unknown user";
            }
        }
    }

    //TODO: add timestamp

    public void addGroup(String name, List<String> topics, String creator, List<String> users) throws SQLException {
        String sqlChat = "insert into chat values(default, ?, true) returning chatID";
        String sqlCreator = "insert into chat_member values(?, ?, 'creator')";
        String sqlMember = "insert into chat_member values(?, ?, 'member')";

        String insertTopic = "INSERT INTO hm_topics (name, is_official) VALUES (?, false) ON CONFLICT (name) DO NOTHING";
        String getTopicId = "SELECT topicID FROM hm_topics WHERE name = ?";
        String linkChat = "INSERT INTO chat_topic (chatID, topicID) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                long chatID;
                try (PreparedStatement ps = con.prepareStatement(sqlChat)) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            chatID = rs.getLong("chatID");
                        } else {
                            throw new SQLException("Failed to create Group: No chatID returned.");
                        }
                    }
                }

                try (PreparedStatement ps1 = con.prepareStatement(sqlCreator)) {
                    ps1.setLong(1, chatID);
                    ps1.setString(2, creator);
                    ps1.executeUpdate();
                }

                try (PreparedStatement ps2 = con.prepareStatement(sqlMember)) {
                    for (String user : users) {
                        ps2.setLong(1, chatID);
                        ps2.setString(2, user);
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }

                if (topics != null && !topics.isEmpty()) {
                    for (String tag : topics) {
                        String cleanTag = tag.trim();
                        if (cleanTag.isEmpty()) continue;

                        try (PreparedStatement p1 = con.prepareStatement(insertTopic)) {
                            p1.setString(1, cleanTag);
                            p1.executeUpdate();
                        }

                        try (PreparedStatement p2 = con.prepareStatement(getTopicId)) {
                            p2.setString(1, cleanTag);
                            try (ResultSet rs = p2.executeQuery()) {
                                if (rs.next()) {
                                    long topicId = rs.getLong("topicID");
                                    try (PreparedStatement p3 = con.prepareStatement(linkChat)) {
                                        p3.setLong(1, chatID);
                                        p3.setLong(2, topicId);
                                        p3.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public String fetchAllGroupsWithTopics() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // Aggregates topics into a comma-separated list for each group
        String sql = """
            SELECT c.chatName, string_agg(t.name, ', ') as topics
            FROM chat c
            JOIN chat_topic ct ON c.chatID = ct.chatID
            JOIN hm_topics t ON ct.topicID = t.topicID
            WHERE c.isGroup = true
            GROUP BY c.chatID, c.chatName
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sb.append("Group Name: '").append(rs.getString("chatName"))
                        .append("' | Topics: ").append(rs.getString("topics")).append("\n");
            }
        }
        return sb.toString();
    }

    public List<String> findTopicsForChat(long chatID) throws SQLException {
        List<String> topics = new ArrayList<>();

        String sql = "SELECT t.name FROM hm_topics t JOIN chat_topic ct ON t.topicID = ct.topicID WHERE ct.chatID = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(rs.getString("name"));
                }
            }
        }
        return topics;
    }

    public void joinGroupByName(String groupName, String username) throws SQLException {
        String sql = """
            INSERT INTO chat_member (chatID, memberID, hm_role) 
            SELECT chatID, ?, 'member' FROM chat WHERE chatName = ? AND isGroup = true LIMIT 1 
            ON CONFLICT DO NOTHING
        """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, groupName);
            ps.executeUpdate();
        }
    }

    public void leaveGroupByName(String groupName, String username) throws SQLException {
        String sql = """
            DELETE from chat_member where 
            chatid = (select chatID from chat where chatName=?) and memberid = ?;
        """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(2, username);
            ps.setString(1, groupName);
            ps.executeUpdate();
        }
    }

    public void touchLastAccess(Long chatId, String username) throws SQLException {
        String sql = "update chat_member set last_access = ? where chatID = ? and memberID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setLong(2, chatId);
            ps.setString(3, username);
            ps.executeUpdate();
        }
    }
}