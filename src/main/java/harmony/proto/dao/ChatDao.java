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

    public boolean isMember(Long chatId, Long userId) throws SQLException {
        String sql = "select 1 from chat_member where chatID = ? and memberID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Long> findMemberIdsByChatId(Long chatId) throws SQLException {
        String sql = "select memberID from chat_member where chatID = ?";
        List<Long> ids = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("memberID"));
                }
            }
        }
        return ids;
    }

    private String findUsersInChat(long chatID) throws SQLException{
        String sql = "select username \n" +
                "from chat_member join hm_user on (userID = memberID) \n" +
                "where chatID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString("username");
            }
        }
    }



    public List<ChatDTO> findUserChats(long userID) throws SQLException{
        //selects all chats that userID is a member of
        String sql = "select chatID, chatName, isGroup, updated_at\n" +
                "from chat join chat_member using (chatID)\n" +
                "where memberID = ?;";
        List<ChatDTO> chatDTOS = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatDTO chatDTO = new ChatDTO();
                    chatDTO.setChatID(rs.getLong("chatID"));
                    chatDTO.setGroup(rs.getBoolean("isGroup"));
//                    chat.setUpdated_at(rs.getTimestamp("updated_at").toInstant());
                    //updated_at is not yet implemented, if null will crash server
                    if(chatDTO.isGroup()){
                        chatDTO.setChatName(rs.getString("chatName"));
                    }
                    else {
                        //if it's a DM, gives chat the name of the other user
                        chatDTO.setChatName(findDmName(userID, chatDTO.getChatID()));
                    }
                    chatDTOS.add(chatDTO);
                }
            }
        }
        return chatDTOS;
    }

    private String findDmName(long userID, long chatID) throws SQLException{
        String sql = "select username \n" +
                "from chat_member join hm_user on (userID = memberID) \n" +
                "where chatID = ? and userID != ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatID);
            ps.setLong(2, userID);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString("username");
            }
        }
    }

    public void touchLastAccess(Long chatId, Long userId) throws SQLException {
        String sql = "update chat_member set last_access = ? where chatID = ? and memberID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setLong(2, chatId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }
}