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

//TODO: every try() block is useless btw lmao

    public List<String> findUsersInChat(long chatID) throws SQLException{
        String sql = "select username \n" +
                "from chat_member join hm_user on (username = memberID) \n" +
                "where chatID = ?";
        List<String> users = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    users.add(rs.getString("username"));
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

    public void addGroup(String name, String creator, List<String> users) throws SQLException{
        String sql = "insert into chat values(default, ?, false) returning chatID";
        long chatID;
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                chatID = rs.getLong("chatID");
            } else {
                throw new SQLException("Failed to create Group: No chatID returned.");
            }
        }

        sql = "insert into chat_member values(" + chatID + ", ?, 'creator') ";
        ps = con.prepareStatement(sql);
        ps.setString(1, creator);
        ps.executeUpdate();

        for(var user:users){
            sql = "insert into chat_member values(" + chatID + ", ?, 'member') ";
            ps = con.prepareStatement(sql);
            ps.setString(1, user);
            ps.addBatch();
        }
        ps.executeBatch();
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