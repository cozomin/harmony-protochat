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

    public void sendFriendReq(String user1, String user2) throws SQLException{
        String sql = "insert into user_friend values( ?, ?, 'outgoing')";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.executeQuery();
        }
    }

    public void acceptFriendReq(String user1, String user2) throws SQLException{
        String sql = "UPDATE user_friend SET status = 'accepted' WHERE user1 = ? and user2 = ?";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.executeQuery();
        }
    }

    public void denyFriendReq(String user1, String user2) throws SQLException{
        String sql = "DELETE from user_friend WHERE user1 = ? and user2 = ?";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.executeQuery();
        }
    }

    public List<UserDTO> fetchFriends(String user1, String status) throws SQLException{
        String sql = "select user2 from user_friend where user1 = ? and status = ?";
        List<UserDTO> users = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    users.add( new UserDTO(rs.getString("user2")));
                }
            }
        }
        return users;
    }
}