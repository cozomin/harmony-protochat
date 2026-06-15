package harmony.proto.dao;

import harmony.proto.dto.MessageDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    private final DataSource dataSource;

    public MessageDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(MessageDTO messageDTO) throws SQLException {
        if (messageDTO.getSentAt() == null) {
            messageDTO.setSentAt(Instant.now());
        }

        String insertMessage = """
                insert into hm_message(senderID, chatID, message_content, sent_at, message_type)
                values (?, ?, ?, ?, ?::mess_enum)
                returning messID
                """;

        //message with parameters
        String updateChat = "update chat set updated_at = ? where chatID = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                //set message parameters
                try (PreparedStatement ps = con.prepareStatement(insertMessage)) {
                    ps.setString(1, messageDTO.getSenderId());
                    ps.setLong(2, messageDTO.getChatId());
                    ps.setString(3, messageDTO.getContent());
                    ps.setTimestamp(4, Timestamp.from(messageDTO.getSentAt()));
                    ps.setString(5, messageDTO.getMessageType());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            messageDTO.setMessId(rs.getLong("messID"));
                        }
                    }
                }
                //set updated_at for the respective chat
                try (PreparedStatement ps = con.prepareStatement(updateChat)) {
                    ps.setTimestamp(1, Timestamp.from(messageDTO.getSentAt()));
                    ps.setLong(2, messageDTO.getChatId());
                    ps.executeUpdate();
                }

                con.commit();
            }catch (SQLException e) {
                    con.rollback();
                    throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }

    }

    public List<MessageDTO> findRecentMessages(Long chatId, int limit) throws SQLException {
        String sql = """
                select messID, senderID, chatID, message_content, sent_at, message_type
                from hm_message
                where chatID = ?
                order by sent_at desc
                limit ?
                """;

        List<MessageDTO> messageDTOS = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MessageDTO m = new MessageDTO();
                    m.setMessId(rs.getLong("messID"));
                    m.setSenderId(rs.getString("senderID"));
                    m.setChatId(rs.getLong("chatID"));
                    m.setContent(rs.getString("message_content"));
                    Timestamp ts = rs.getTimestamp("sent_at");
                    m.setSentAt(ts == null ? Instant.now() : ts.toInstant());
                    m.setMessageType(rs.getString("message_type"));
                    messageDTOS.add(m);
                }
            }
        }

        messageDTOS.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
        return messageDTOS;
    }

    public void editMessage(Long messId, String newContent) throws SQLException {
        String sql = "update hm_message set message_content = ? where messID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setLong(2, messId);
            ps.executeUpdate();
        }
    }

    public void deleteMessage(Long messId) throws SQLException {
        String sql = "delete from hm_message where messID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, messId);
            ps.executeUpdate();
        }
    }
}