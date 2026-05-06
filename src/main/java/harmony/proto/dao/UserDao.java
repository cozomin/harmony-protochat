package harmony.proto.dao;

import harmony.proto.dto.UserDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "select 1 from hm_user where username = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
        }
        //if query returns nothing, function returns FALSE
    }

    public String login(String username, String pass) throws SQLException{
        //checks if userID has inserted correct password
        String sql = "select username from hm_user where username = ? and pass = ?";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);){
            ps.setString(1, username);
            ps.setString(2, pass);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next())
                    return username;
                else
                    return null;
            }
        }
    }

    public String signUp(String username, String pass) throws SQLException{
        String sql = "insert into hm_user values(?, ?)";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);){
            ps.setString(1, username);
            ps.setString(2, pass);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0){
                return username;
            } else {
                return null;
            }
        }
    }

    public void addDM(String user1, String user2) throws SQLException{
        String sql = "insert into chat values(default, default, false) returning chatID";
        Long chatID;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    chatID = rs.getLong("chatID");
                } else {
                    throw new SQLException("Failed to create DM: No chatID returned.");
                }
            }
        }
        sql = "insert into chat_member values (?, ?, 'member');";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatID);
            ps.setString(2, user1);
            ps.addBatch();

            if (!user1.equals(user2)) {
                ps.setLong(1, chatID);
                ps.setString(2, user2);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void sendFriendReq(String user1, String user2) throws SQLException{
        String sql = "insert into user_friend values( ?, ?, 'outgoing')";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.executeUpdate();
        }
        catch(SQLException e){
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("Friendship already exists or request is pending!");
            }
            throw e;
        }
    }

    public boolean dmExists(String user1, String user2) throws SQLException {
        String sql = "select c.chatID " +
                "from chat c join chat_member m1 on (c.chatID = m1.chatID) join chat_member m2 on c.chatID = m2.chatID " +
                "where c.isGroup = false and m1.memberID = ? and m2.memberID = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user1);
            ps.setString(2, user2);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void acceptFriendReq(String user1, String user2) throws SQLException{
        String sql = "UPDATE user_friend SET status = 'accepted' WHERE user1 = ? and user2 = ?";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(2, user1);
            ps.setString(1, user2);
            ps.executeUpdate();
        }
        if(!dmExists(user1, user2)) addDM(user1, user2);
    }

    public void denyFriendReq(String user1, String user2) throws SQLException{
        String sql = "DELETE from user_friend WHERE (user1 = ? and user2 = ?) or (user1 = ? and user2 = ?)";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);
            ps.executeUpdate();
        }
    }

    public int friendExists(String user1, String user2) throws SQLException{
        String sqlAcc = "select 1 from user_friend where ((user1 = ? and user2 = ?) or (user1 = ? and user2 = ?)) and status = 'accepted' ";
        String sqlNotAcc = "select 1 from user_friend where ((user1 = ? and user2 = ?) or (user1 = ? and user2 = ?)) and status != 'accepted' ";

        try(Connection con = dataSource.getConnection();){
            try(PreparedStatement ps = con.prepareStatement(sqlAcc);) {

                ps.setString(1, user1);
                ps.setString(2, user2);
                ps.setString(3, user2);
                ps.setString(4, user1);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return 1;
                }
            }

            try(PreparedStatement ps = con.prepareStatement(sqlNotAcc);) {

                ps.setString(1, user1);
                ps.setString(2, user2);
                ps.setString(3, user2);
                ps.setString(4, user1);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return 2;
                }
            }

            return 0;
        }
    }

    public List<UserDTO> fetchFriends(String username, String fetchType) throws SQLException {
        String sql = getString(fetchType);

        List<UserDTO> users = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            if ("accepted".equals(fetchType)) {
                ps.setString(2, username);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    users.add(new UserDTO(rs.getString("friend")));
                }
            }
        }
        return users;
    }
    //TODO: incoming is useless
    private static String getString(String fetchType) {
        String sql;

        if ("accepted".equals(fetchType)) {
            sql = "select user2 as friend from user_friend where user1 = ? and status = 'accepted' " +
                    "union " +
                    "select user1 as friend from user_friend where user2 = ? and status = 'accepted'";
        }
        else if ("outgoing".equals(fetchType)) {
            sql = "select user2 as friend from user_friend where user1 = ? and status = 'outgoing'";
        }
        else {//incoming
            sql = "select user1 as friend from user_friend where user2 = ? and status = 'outgoing'";
        }
        return sql;
    }

//    public List<UserDTO> fetchFriends(String user1, String status) throws SQLException{
//        String sql = "select user2 from user_friend where user1 = ? and status = ? " +
//                "union " +
//                "select user1 from user_friend where user2 = ? and status = ?";
//        List<UserDTO> users = new ArrayList<>();
//        try (Connection con = dataSource.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setString(1, user1);
//            ps.setString(2, status);
//            try (ResultSet rs = ps.executeQuery()) {
//                while(rs.next()) {
//                    users.add( new UserDTO(rs.getString("user2")));
//                }
//            }
//        }
//        return users;
//    }
}