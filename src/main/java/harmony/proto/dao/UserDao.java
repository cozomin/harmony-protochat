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

    public boolean existsById(Long userId) throws SQLException {
        String sql = "select 1 from hm_user where userID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
        //if query returns nothing, function returns FALSE
    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "select 1 from hm_user where username = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
        //if query returns nothing, function returns FALSE
    }

    public Long login(String username, String pass) throws SQLException{
        //checks if userID has inserted correct password
        String sql = "select userID from hm_user where username = ? and pass = ?";
        try(Connection con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);){
            ps.setString(1, username);
            ps.setString(2, pass);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next())
                    return rs.getLong("userID");
                else
                    return (long) -1;
            }
        }
    }
}