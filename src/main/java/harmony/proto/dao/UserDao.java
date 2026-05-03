package harmony.proto.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String sql = "insert into hm_user values(default, ?, ?)";
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
}