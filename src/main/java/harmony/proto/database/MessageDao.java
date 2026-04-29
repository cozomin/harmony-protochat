package harmony.proto.database;

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

    public Message save(Message message) throws SQLException {
        String insertMessage = """
                insert into hm_message(senderID, chatID, message_content, sent_at, message_type)
                values (?, ?, ?, ?, ?::mess_enum)
                returning messID
                """;
        //message with parameters
        String updateChat = "update chat set updated_at = ? where chatID = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            //set message parameters
            try (PreparedStatement ps = con.prepareStatement(insertMessage)) {
                ps.setLong(1, message.getSenderId());
                ps.setLong(2, message.getChatId());
                ps.setString(3, message.getContent());
                ps.setTimestamp(4, Timestamp.from(message.getSentAt()));
                ps.setString(5, message.getMessageType());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        message.setMessId(rs.getLong("messID"));
                    }
                }
            }
            //set updated_at for the respective chat
            try (PreparedStatement ps = con.prepareStatement(updateChat)) {
                ps.setTimestamp(1, Timestamp.from(message.getSentAt()));
                ps.setLong(2, message.getChatId());
                ps.executeUpdate();
            }

            con.commit();
            return message;
        }
    }

    public List<Message> findRecentMessages(Long chatId, int limit) throws SQLException {
        String sql = """
                select messID, senderID, chatID, message_content, sent_at, message_type
                from hm_message
                where chatID = ?
                order by sent_at desc
                limit ?
                """;

        List<Message> messages = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessId(rs.getLong("messID"));
                    m.setSenderId(rs.getLong("senderID"));
                    m.setChatId(rs.getLong("chatID"));
                    m.setContent(rs.getString("message_content"));
                    Timestamp ts = rs.getTimestamp("sent_at");
                    m.setSentAt(ts == null ? Instant.now() : ts.toInstant());
                    m.setMessageType(rs.getString("message_type"));
                    messages.add(m);
                }
            }
        }

        messages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
        return messages;
    }
}